package API::HwInfo;
#
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#
#
use UI::Utils;
use Mojo::Base 'Mojolicious::Controller';

sub index {
	my $self    = shift;
	my $orderby = $self->param('orderby') || "serverid";
	my $limit   = $self->param('limit') || 1000;
	my @data;

	# get list of servers in one query
	my $rs_data =
		$self->db->resultset("Hwinfo")->search( undef, { prefetch => [ { 'serverid' => undef, } ], order_by => 'me.' . $orderby, rows => $limit } );
	while ( my $row = $rs_data->next ) {
		my $id = $row->id;
		push(
			@data, {
				"serverId"       => $row->serverid->id,
				"serverHostName" => $row->serverid->host_name,
				"description"    => $row->description,
				"val"            => $row->val,
				"lastUpdated"    => $row->last_updated,
			}
		);
	}

	$limit += 0; #converts to int.
	$self->success( \@data, undef, undef, $limit, undef );
}

sub data {
	my $self           = shift;
	my $idisplay_start = $self->param("iDisplayStart");
	my $sort_order     = $self->param("sSortDir_0") || "asc";
	my $search_field   = $self->param("sSearch");
	my $sort_column    = $self->param("iSortCol_0");

	# NOTE: If changes are made to send additional columns then this mapping has to be updated
	# to match the Column Number coming from datatables to it's name
	# Unfortunately, this is a short coming with the jquery datatables ui widget in that it expects
	# an array arrays instead of an array of hashes
	my $sort_direction        = sprintf( "-%s", $sort_order );
	my @column_number_to_name = qw{ serverid.host_name description val last_updated };
	my $column_name           = $column_number_to_name[ $sort_column - 1 ] || "serverid";

	my $idisplay_length = $self->param("iDisplayLength");

	my %data = ( "data" => [] );
	my $rs;

	my %condition;
	my %attrs;
	my %limit;

	#if (search_field eq ''){
	%condition = (
		-or => {
			'me.description'     => { -like => '%' . $search_field . '%' },
			'me.val'             => { -like => '%' . $search_field . '%' },
			'serverid.host_name' => { -like => '%' . $search_field . '%' }
		}
	);

	#} else {
	#}
	%limit = ( order_by => { $sort_direction => $column_name }, page => $idisplay_start, rows => $idisplay_length );
	%attrs = ( attrs => [ { 'serverid' => undef } ], join => 'serverid', %limit );

	# Original
	#$rs = $self->db->resultset('Hwinfo')->search( undef, { attrs => ['serverid'] } );
	$rs = $self->db->resultset('Hwinfo')->search( \%condition, \%attrs );

	while ( my $row = $rs->next ) {
		my @line = [ $row->serverid->id, $row->serverid->host_name . "." . $row->serverid->domain_name, $row->description, $row->val, $row->last_updated ];
		push( @{ $data{'data'} }, @line );
	}
	my $total_display_records = 0;
	if ( defined( $data{"data"} ) ) {
		$total_display_records = scalar @{ $data{"data"} };
	}
	my %itotals_display_records = ( iTotalRecords => $total_display_records );
	%data = %{ merge( \%data, \%itotals_display_records ) };

	# Count all records
	if ( $search_field eq '' ) {
		$rs = $self->db->resultset('Hwinfo')->search();
	}
	else {
		$rs = $self->db->resultset('Hwinfo')->search(
			{
				-or => {
					'me.description'     => { -like => '%' . $search_field . '%' },
					'me.val'             => { -like => '%' . $search_field . '%' },
					'serverid.host_name' => { -like => '%' . $search_field . '%' }
				}
			},
			{ page => $idisplay_start, prefetch => [ { 'serverid' => undef } ], join => 'serverid' },
		);

	}

	my $total_records = $rs->count;
	my %itotal_records = ( iTotalDisplayRecords => $total_records );
	%data = %{ merge( \%data, \%itotal_records ) };

	$self->render( json => \%data );
}

sub update {
        my $self   = shift;
        my $params = $self->req->json;

        if ( !&is_oper($self) ) {
                return $self->forbidden();
        }

        my $hwinfo = $self->db->resultset('Hwinfo')->find( { serverid => $params->{serverid} } );
        if ( !defined($hwinfo) ) {
                return $self->not_found();
        }

        if ( !defined($params) ) {
                return $self->alert("parameters must be in JSON format.");
        }

        if ( !defined( $params->{description} ) ) {
                return $self->alert("Description is required.");
        }

        my $values = { val => $params->{val} };

        my $rs = $hwinfo->update($values);
        if ($rs) {
                my $response;
                $response->{serverid}    = $rs->serverid;
                $response->{description} = $rs->description;
                $response->{val}         = $rs->val;
                $response->{lastUpdated} = $rs->last_updated;
                &log( $self, "Updated Description '" . $rs->description . "' for id: " . $rs->serverid, "APICHANGE" );
                return $self->success( $response, "Hardware update was successful." );
        }
        else {
                return $self->alert("Hardware update failed.");
        }

}

sub create {
        my $self   = shift;
        my $params = $self->req->json;
        if ( !defined($params) ) {
                return $self->alert("parameters must be in JSON format,  please check!");
        }

        if ( !&is_oper($self) ) {
                return $self->alert( { Error => " - You must be an ADMIN or OPER to perform this operation!" } );
        }

        my $description = $params->{description};
        my $serverid    = $params->{serverid};
        if ( !defined($description) || !defined($serverid) ) {
                return $self->alert("hwinfo 'description' and/or 'serverid' is not given.");
        }

        # Check for duplicate serverid/description name
        my $existing_hwinfo = $self->db->resultset('Hwinfo')->search( {  -and => ['serverid' => $serverid, 'description' => $description ] })->single();
        if ($existing_hwinfo) {
                return $self->alert("A serverid \"$serverid\" and description \"$description\" already exists.");
        }

        my $values = {
                serverid        => $params->{serverid} ,
                description     => $params->{description} ,
                val             => $params->{val}
        };

        my $insert = $self->db->resultset('Hwinfo')->create($values);
        my $rs = $insert->insert();

        # TODO, add hostname
        if ($rs) {
                my $response;
                $response->{serverid}           = $rs->serverid;
                $response->{description}        = $rs->description;
                $response->{val}                = $rs->val;
                $response->{lastUpdated}        = $rs->last_updated;

                &log( $self, "Created hardware info name '" . $rs->description . "' for id: " . $rs->serverid, "APICHANGE" );

                return $self->success( $response, "Hardware Info create was successful." );
        }
        else {
                return $self->alert("Hardware Info create failed.");
        }
}

sub delete {
        my $self = shift;
        my $params = $self->req->json;

        if ( !defined($params) ) {
                return $self->alert("parameters must be in JSON format,  please check!");
        }

        if ( !&is_oper($self) ) {
                return $self->forbidden();
        }

        my $hwinfo = $self->db->resultset('Hwinfo')->find( { serverid => $params->{serverid} } );
        if ( !defined($hwinfo) ) {
                return $self->not_found();
        }

        my $rs = $hwinfo->delete();
        if ($rs) {
                return $self->success_message("Hardware Info for server" . $params->{serverid} . " deleted.");
        } else {
                return $self->alert( "Hardware Info delete failed." );
        }
}



1;
