/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.comcast.cdn.traffic_control.traffic_router.jdnssec.dns.keys;

import com.comcast.cdn.traffic_control.traffic_router.shared.IsEqualCollection;
import com.comcast.cdn.traffic_control.traffic_router.core.dns.DnsSecKeyPair;
import com.comcast.cdn.traffic_control.traffic_router.core.dns.DnsSecKeyPairImpl;
import com.comcast.cdn.traffic_control.traffic_router.core.dns.ZoneSignerImpl;
import com.comcast.cdn.traffic_control.traffic_router.shared.SigningData;
import com.comcast.cdn.traffic_control.traffic_router.shared.ZoneTestRecords;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verisignlabs.dnssec.security.DnsKeyPair;
import com.verisignlabs.dnssec.security.JCEDnsSecSigner;
import com.verisignlabs.dnssec.security.SignUtils;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.DSRecord;
import org.xbill.DNS.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.comcast.cdn.traffic_control.traffic_router.shared.IsEqualCollection.equalTo;
import static com.comcast.cdn.traffic_control.traffic_router.shared.ZoneTestRecords.keySigningKeyRecord;
import static com.comcast.cdn.traffic_control.traffic_router.shared.ZoneTestRecords.origin;
import static com.comcast.cdn.traffic_control.traffic_router.shared.ZoneTestRecords.sep_1_2016;
import static com.comcast.cdn.traffic_control.traffic_router.shared.ZoneTestRecords.sep_1_2026;
import static com.comcast.cdn.traffic_control.traffic_router.shared.ZoneTestRecords.zoneSigningKeyRecord;
import static java.util.Arrays.asList;
import static java.util.Base64.getMimeDecoder;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertThat;
import static org.xbill.DNS.DSRecord.SHA256_DIGEST_ID;

public class ZoneSignerTest {

	private DnsKeyPair kskPair1;
	private DnsKeyPair kskPair2;
	private DnsKeyPair zskPair1;
	private DnsKeyPair zskPair2;
	private JsonNode ksk1Json;
	private JsonNode ksk2Json;
	private JsonNode zsk1Json;
	private JsonNode zsk2Json;
	private final long dsTtl = 1234000L;

	private String decodePrivateKeyString(String encodedString) {
		return new String(getMimeDecoder().decode(encodedString.getBytes()));
	}
	@Before
	public void before() throws Exception {
		ZoneTestRecords.generateZoneRecords(false);
		SigningData.recreateData();

		final ObjectMapper mapper = new ObjectMapper();

		kskPair1 = new DnsKeyPair(keySigningKeyRecord, decodePrivateKeyString(SigningData.ksk1Private));
		kskPair2 = new DnsKeyPair(keySigningKeyRecord, decodePrivateKeyString(SigningData.ksk2Private));
		zskPair1 = new DnsKeyPair(zoneSigningKeyRecord, decodePrivateKeyString(SigningData.zsk1Private));
		zskPair2 = new DnsKeyPair(zoneSigningKeyRecord, decodePrivateKeyString(SigningData.zsk2Private));

		// Data like we would fetch from traffic ops api for dnsseckeys.json
		String s = "{" +
				"\n\t\"inceptionDate\":1475280000," +
				"\n\t\"effectiveDate\": 1475280000," +
				"\n\t\"expirationDate\": 1790812800," +
				"\n\t\"ttl\": 3600," +
				"\n\t\"name\":\"example.com.\"," +
				"\n\t\"private\": \"" + SigningData.ksk1Private.replaceAll("\n", "\\\\n") + "\"," +
				"\n\t\"public\": \"" + SigningData.keyDnsKeyRecord.replaceAll("\n", "\\\\n") + "\"" +
				"\n}";
		ksk1Json = mapper.readTree(s);

		s = "{" +
				"\n\t\"inceptionDate\":1475280000," +
				"\n\t\"effectiveDate\": 1475280000," +
				"\n\t\"expirationDate\": 1790812800," +
				"\n\t\"ttl\": 3600," +
				"\n\t\"name\":\"example.com.\"," +
				"\n\t\"private\": \"" + SigningData.ksk2Private.replaceAll("\n", "\\\\n") + "\"," +
				"\n\t\"public\": \"" + SigningData.keyDnsKeyRecord.replaceAll("\n", "\\\\n") + "\"" +
				"\n}";
		ksk2Json = mapper.readTree(s);

		s = "{" +
				"\n\t\"inceptionDate\":1475280000," +
				"\n\t\"effectiveDate\": 1475280000," +
				"\n\t\"expirationDate\": 1790812800," +
				"\n\t\"ttl\": 31556952," +
				"\n\t\"name\":\"example.com.\"," +
				"\n\t\"private\": \"" + SigningData.zsk1Private.replaceAll("\n", "\\\\n") + "\"," +
				"\n\t\"public\": \"" + SigningData.zoneDnsKeyRecord.replaceAll("\n", "\\\\n") + "\"" +
				"\n}";
		zsk1Json = mapper.readTree(s);

		s = "{" +
				"\n\t\"inceptionDate\":1475280000," +
				"\n\t\"effectiveDate\": 1475280000," +
				"\n\t\"expirationDate\": 1790812800," +
				"\n\t\"ttl\": 315569520," +
				"\n\t\"name\":\"example.com.\"," +
				"\n\t\"private\": \"" + SigningData.zsk2Private.replaceAll("\n", "\\\\n") + "\"," +
				"\n\t\"public\": \"" + SigningData.zoneDnsKeyRecord.replaceAll("\n", "\\\\n") + "\"" +
				"\n}";
		zsk2Json = mapper.readTree(s);
	}

	@Test
	public void itCanReproduceResultsDirectlyFromJdnsSec() throws Exception {
		List<DnsKeyPair> kskPairs = new ArrayList<>(asList(kskPair1, kskPair2));
		List<DnsKeyPair> zskPairs = new ArrayList<>(asList(zskPair1, zskPair2));

		JCEDnsSecSigner signer = new JCEDnsSecSigner(false);

		final List<Record> signedRecords = signer.signZone(origin, ZoneTestRecords.records,
			kskPairs, zskPairs, sep_1_2016, sep_1_2026, true, SHA256_DIGEST_ID);

		assertThat(signedRecords, equalTo(SigningData.signedList));
		assertThat(ZoneTestRecords.records, equalTo(SigningData.postZoneList));
	}

	@Test
	public void itReturnsSameResults() throws Exception {
		DNSKeyPairWrapper ksk1Wrapper = new DNSKeyPairWrapper(ksk1Json, 1234);

		assertThat(ksk1Wrapper.getDNSKEYRecord(), equalTo(kskPair1.getDNSKEYRecord()));

		DNSKeyPairWrapper ksk2Wrapper = new DNSKeyPairWrapper(ksk2Json, 1234);

		assertThat(ksk2Wrapper.getDNSKEYRecord(), equalTo(kskPair2.getDNSKEYRecord()));

		List<DnsSecKeyPair> kskWrapperPairs = new ArrayList<>(asList(ksk1Wrapper, ksk2Wrapper));

		DNSKeyPairWrapper zsk1Wrapper = new DNSKeyPairWrapper(zsk1Json, 1234);

		assertThat(zsk1Wrapper.getDNSKEYRecord(), equalTo(zskPair1.getDNSKEYRecord()));

		DNSKeyPairWrapper zsk2Wrapper = new DNSKeyPairWrapper(zsk2Json, 1234);

		assertThat(zsk2Wrapper.getDNSKEYRecord(), equalTo(zskPair2.getDNSKEYRecord()));

		List<DnsSecKeyPair> zskWrapperPairs = new ArrayList<>(asList(zsk1Wrapper, zsk2Wrapper));

		final List<Record> signedRecords2 = new JDnsSecSigner().signZone(origin, ZoneTestRecords.records,
			kskWrapperPairs, zskWrapperPairs, sep_1_2016, sep_1_2026, true, SHA256_DIGEST_ID);

		assertThat(signedRecords2, equalTo(SigningData.signedList));
		assertThat(ZoneTestRecords.records, equalTo(SigningData.postZoneList));
	}

	@Test
	public void itReturnsTheSameResultsWithoutJDnsSec() throws Exception {
		DnsSecKeyPair kskPair1 = new DnsSecKeyPairImpl(ksk1Json, 1234);
		DnsSecKeyPair kskPair2 = new DnsSecKeyPairImpl(ksk2Json, 1234);
		DnsSecKeyPair zskPair1 = new DnsSecKeyPairImpl(zsk1Json, 1234);
		DnsSecKeyPair zskPair2 = new DnsSecKeyPairImpl(zsk2Json, 1234);

		List<DnsSecKeyPair> kskPairs = new ArrayList<>(asList(kskPair1, kskPair2));
		List<DnsSecKeyPair> zskPairs = new ArrayList<>(asList(zskPair1, zskPair2));

		final List<Record> signedRecords = new ZoneSignerImpl().signZone(origin, ZoneTestRecords.records,
			kskPairs, zskPairs, sep_1_2016, sep_1_2026, true, SHA256_DIGEST_ID);

		assertThat("Signed records not equal", signedRecords, equalTo(SigningData.signedList));
		assertThat("Post Zone Records not equal", ZoneTestRecords.records, equalTo(SigningData.postZoneList));
	}

	@Test
	public void itCanReproduceDSRecordsFromJdnsSec() throws Exception {
		List<DnsKeyPair> kskPairs = new ArrayList<>(asList(kskPair1, kskPair2));
		List<DSRecord> dsRecords = kskPairs.stream()
			.map(dnsKeyPair -> SignUtils.calculateDSRecord(dnsKeyPair.getDNSKEYRecord(), SHA256_DIGEST_ID, dsTtl))
			.collect(toList());

		assertThat(dsRecords, IsEqualCollection.equalTo(SigningData.dsRecordList));
	}

	@Test
	public void itReturnsSameDSRecords() throws Exception {
		DnsSecKeyPair kskPair1 = new DnsSecKeyPairImpl(ksk1Json, 1234);
		DnsSecKeyPair kskPair2 = new DnsSecKeyPairImpl(ksk2Json, 1234);

		List<DSRecord> dsRecords = Stream.of(kskPair1, kskPair2)
			.map(dnsSecKeyPair -> new ZoneSignerImpl().calculateDSRecord(kskPair1.getDNSKEYRecord(), SHA256_DIGEST_ID, 54321L))
			.collect(toList());
		assertThat(dsRecords, IsEqualCollection.equalTo(SigningData.dsRecordList));
	}
}
