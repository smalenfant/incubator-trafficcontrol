/*

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

-- +goose Up
-- SQL in section 'Up' is executed when this migration is applied
CREATE TABLE deliveryservice_request_comment (
    author_id bigint NOT NULL,
    deliveryservice_request_id bigint NOT NULL,
    id bigserial primary key NOT NULL,
    last_updated timestamp WITH time zone NOT NULL DEFAULT now(),
    value text NOT NULL
);

ALTER TABLE deliveryservice_request_comment
    ADD CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES tm_user(id) ON DELETE CASCADE;

ALTER TABLE deliveryservice_request_comment
    ADD CONSTRAINT fk_deliveryservice_request FOREIGN KEY (deliveryservice_request_id) REFERENCES deliveryservice_request(id) ON DELETE CASCADE;

CREATE TRIGGER on_update_current_timestamp BEFORE UPDATE ON deliveryservice_request_comment FOR EACH ROW EXECUTE PROCEDURE on_update_current_timestamp_last_updated();

-- +goose Down
-- SQL section 'Down' is executed when this migration is rolled back
DROP TABLE deliveryservice_request_comment;
