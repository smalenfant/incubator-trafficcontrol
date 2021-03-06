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

package secure;

import com.comcast.cdn.traffic_control.traffic_router.secure.CertificateDataConverter;
import com.comcast.cdn.traffic_control.traffic_router.secure.HandshakeData;
import com.comcast.cdn.traffic_control.traffic_router.shared.Certificate;
import com.comcast.cdn.traffic_control.traffic_router.shared.CertificateData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.time.Instant;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

public class CertificateDataConverterTest {

	private CertificateDataConverter certificateDataConverter;
	private CertificateData certificateData;
	private Date certDate;
	private final static String SUBJECT_MISS_CERT_DATA =
		"    {\n" +
			"      \"deliveryservice\": \"https-subject-miss\",\n" +
			"      \"certificate\": {\n" +
			"        \"comment\" : \"The following is a self-signed key for *.subject-miss.thecdn.example.com\",\n" +
			"        \"key\": \"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUV2QUlCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktZd2dnU2lBZ0VBQW9JQkFRQzhBWVVFYk1YcHZiVUMKaDBrNWRxYURnTHJGL3Y5VDdtOFNLUnVuRldYYUhFalVvcWlZc29tekhuZjNyUkVNRWpkVXB0M0lCVzk3M090cApqNmlkNUNLTHlFVDNUQ3h2ZHNERzhiYXB3UEdNT0dzQWhTMGxucmlrRll6ejArZXpxMWhzczcxRDBqN3o1TzlLCmxPVUJxSUgzOG16YU1JaFN3VXpsSGdFRzJjdlJiK1RwajhpU0k3Z3psek8rMVM1OEExS21UbjVDMC9ia0lvcFYKREJ5V3FySmpqSXZuWjBvK2I1MkRMcExzdlVnRU5BOVdHRzkycG8wS0RDZnFmNjN0RW5oRGYvZStFT0o5NUs5UQpCUG45YW82OVJaM0V3cDk5bnlveDJ6cmtHLzcvMTVIV3Z5aUVzQUR2TWxNaTg4bTJRTzBOaDA5ZWlrWWFRWDlVCkUzbTM4VDVkQWdNQkFBRUNnZ0VBT2UxNTc4Z1lIeElkMEw2Z2VEMHZ4enNGMFhYbGRCWDJVVEVyWFFzQnkvZUYKRlVkZERWZU5pQXd1U0xraGxJZVVWdGZuWS9jUXg2aGxQS3hQOXY1UkNxTFZaU0VxVzluS1FrSTkxd1lsSnVCSApUK3k0NFd1TFZydHhKN3UyRzYwQzNOTncwSkhhWmNtM1ZWS1ZVVEo3Z1V0SDhONmRVbXBPNkJXYm1XSElKQ3AvCnRjL29QVTZzTWc2RGh2MVFxeUpJeHQ5MWRmSnpBZVdkV01MM3ZmNnRVUDF6bTh2M2g0WXBZSGR5LzZBMzZjZkQKa0xnZkkybktkVEhLUDBldlZFS3M5L3hQWVlxQWVyTnlCV2NFWCtjVy93ZzVSVUVrT2lpajZUY0h1cmVnV09VbQp5cWlCOFNoQWVwdEtnN0VVaHZ2V2ZLSEtMTmJURUV5UE5GOXVPR1VqL1FLQmdRRGZUdy9IbS9oUDJYZUFGeEhZCnViUTBIN2xHQWxLZEhydTIvVXlFK3d0WDlEYXU1UTZRVzVkVzJJMTdkOG5TL2taUkloQnhSM08rMGMxN3VUaHoKWDFseWtmT3ZZb3NlSGhhN0IrN21rL2RVTkJZYVJ1UG1IcEEvTkx6ekI3OWdhU2JPVk9lOVQ5cVovb2lndlRXYQp0TG0rOFIzeVhyUktOdUZtNk1kbnFYdSswd0tCZ1FEWGgybU5EQW9sMHJlcFY0MnYvaSt1QmF4bkozZlJVU284ClpkUk5GczVWTFlxUnh0c1BubkNwcTh3OWtKK2paZWNWTkRNelB3dmpBdUdqYXkyUHVRclF5MEUyckk5RExITEIKTVVxVHJENHBSN0NrK3VFVGQ3ZlBEOHNRdEJmUkp6amdBa3pvWUk1djZ1dzlpUFF6U2tzT2d5WFlNV0ZUU1ZJNwplVVhHWDRDd0R3S0JnRjduemhBS2xKenpFcHVvc2xnR2pMUythdEo3T0RzNGpaVDIwQ2VRUGtEeU5LOWVBRE9RCkNhREtSazhjR1BXSVJjQkRsdk5kNTY1SW9ta2J6Z2NTbGdSZ1RVM1R0c1psQ1VvUjFCSEEveE9WVTNOMWYzUVUKdHo5MW5YdzRaYmlHMkF4Ry8zcHd6cm8xK0VGQVNPRG9RQzBMY3F2SVhoMVFkN2x4NHhXR2JXWXJBb0dBQjhZKwpySFBPdWVhTDhYUFRESklpcmloT083cFV2Qnd0WmRoV2ZDRmlkL2dZazRHVXpVOXR5UEVGZ1FNQ2Z5WmgyNFh5Cmd0cTNWd3ozanFtRER6Z2hoNzZOTDZleDB6NTdOVFROOTkyeXNGS0JzTEhNQktQQTRaczBPL29ERWV4VVJPQlEKWGVGOXdkTzdpY3l5NGxhL3RscE10eXV3MHd4R0J4Y3N5U2NRd1VrQ2dZQng0UGk4NkhmWkVnT3p4bU9NYkhGTApmeFFWbXpZL0Y3eCsyOGMrQWQ3VVZRMVVjcDRUdEdMT1pHUGRIYnZzR2dZeXY4cSs0MTcwK1M0YkY4bC9JRThnCjJBNzl6VzNjMjhVNzR0KytxQ0p4bS82SmxtR3RCQkt0Mk9ZSE5ocUdRQ2Z4Y0krWGFPQUgyUUFNSS9zZ3JzOXQKM3dZNlY2VUQ2K1lCTDVFRFp6T2NMQT09Ci0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\n" +
			"        \"crt\": \"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURvakNDQW9vQ0NRQ1JFTVdLWEhZYkhUQU5CZ2txaGtpRzl3MEJBUXNGQURDQmtURUxNQWtHQTFVRUJoTUMKVlZNeEN6QUpCZ05WQkFnTUFrTlBNUXd3Q2dZRFZRUUhEQU5FUlU0eEN6QUpCZ05WQkFvTUFsUkRNUXN3Q1FZRApWUVFMREFKVVF6RXFNQ2dHQTFVRUF3d2hLaTV6ZFdKcVpXTjBMVzFwYzNNdWRHaGxZMlJ1TG1WNFlXMXdiR1V1ClkyOXRNU0V3SHdZSktvWklodmNOQVFrQkZoSjBZMEJ6Wld4bUxYTnBaMjVsWkM1amIyMHdJQmNOTVRrd016QTEKTVRjME1ETXhXaGdQTWpFeE9UQXlNRGt4TnpRd016RmFNSUdSTVFzd0NRWURWUVFHRXdKVlV6RUxNQWtHQTFVRQpDQXdDUTA4eEREQUtCZ05WQkFjTUEwUkZUakVMTUFrR0ExVUVDZ3dDVkVNeEN6QUpCZ05WQkFzTUFsUkRNU293CktBWURWUVFERENFcUxuTjFZbXBsWTNRdGJXbHpjeTUwYUdWalpHNHVaWGhoYlhCc1pTNWpiMjB4SVRBZkJna3EKaGtpRzl3MEJDUUVXRW5SalFITmxiR1l0YzJsbmJtVmtMbU52YlRDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRApnZ0VQQURDQ0FRb0NnZ0VCQUx3QmhRUnN4ZW05dFFLSFNUbDJwb09BdXNYKy8xUHVieElwRzZjVlpkb2NTTlNpCnFKaXlpYk1lZC9ldEVRd1NOMVNtM2NnRmIzdmM2Mm1QcUoza0lvdklSUGRNTEc5MndNYnh0cW5BOFl3NGF3Q0YKTFNXZXVLUVZqUFBUNTdPcldHeXp2VVBTUHZQazcwcVU1UUdvZ2ZmeWJOb3dpRkxCVE9VZUFRYlp5OUZ2NU9tUAp5SklqdURPWE03N1ZMbndEVXFaT2ZrTFQ5dVFpaWxVTUhKYXFzbU9NaStkblNqNXZuWU11a3V5OVNBUTBEMVlZCmIzYW1qUW9NSitwL3JlMFNlRU4vOTc0UTRuM2tyMUFFK2YxcWpyMUZuY1RDbjMyZktqSGJPdVFiL3YvWGtkYS8KS0lTd0FPOHlVeUx6eWJaQTdRMkhUMTZLUmhwQmYxUVRlYmZ4UGwwQ0F3RUFBVEFOQmdrcWhraUc5dzBCQVFzRgpBQU9DQVFFQXJBTWNyZWY0bTNVTlNSRU54dW0xWTNXYlgzWU91VTdLVyt4bEV2UGVHLzFmYitFRUNrcjg5dXFZCm95OWFWTjYvK3RMTWd5Y1QxL1cxVnhSNkl5bFpZcE9SRFRVZ1c4L3ZvSUROVUZack1VekR6RmZNVlFwdUxyUzkKSk5kejk2aTFrNjdBMGRrdFBURjExam5DZEY2VVhKMHdZTmZCVEIvbXo1T2diWVdaQWsrYW5pTTR5NUwyb3ZaKwpLOEZUVi8wUHpIUWRVTkVGVjN4QzVRcVB3aW1oY3BrbDU1bzJnTjVUNXllVnBDekRYeHhSWWR0YjRUYmsvVDF5CnlBVkhQQ3ZFeENnbXcrUW5TS1VweXRKQ0NqM3pOS01FY1V5dkxFOUFNTmlrdFJpY2c4NG5UTk9hWWRnM1ZaUk8KMVRYY3ZNb0l5NFNOZUk0NEszTkhsQW1IdEJkY0VBPT0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo=\"\n" +
			"      },\n" +
			"      \"hostname\": \"*.https-subject-miss.thecdn.example.com\"\n" +
		"    }";
	private final static String VALID_CERT_DATA =
		"    {\n" +
			"      \"deliveryservice\": \"https-valid-test\",\n" +
			"      \"certificate\": {\n" +
			"        \"comment\" : \"The following is just a self signed certificate and key to use for testing this is NOT private data from a CA\",\n" +
			"        \"key\": " +
					"\"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUV2Z0lCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktnd2dnU2tBZ0VBQW9JQkFRQzVEMVhNbXJiQy9CT1gKUkZMVkczbTNSbmhWZ0ZJdUQ5dXhWSEJDYXR2TEFuc2ZyalhCM2tyZjVNTDVuS3dZRWl3OCtkQWo2N1Z2QkR4cwpDMTYvbFFBbFM4YnBxT1NRbzU5T0RDcVBNZmZaYzVVazdVdjUzN0R5MWFHMjRiT1R0eUxjQzIxc2MxSm1YWHVjCkVlQlZUZldFWUVLdS9McHEvZDZZUlNsa1lXUUt2TDBmUzRja0FtcUJkRVk2Q0s3ajZyYnphZGJIVHB2SXdQWGgKWVNTOWlJOFQxKzRTYTZDOTljcnRUR2ZZb21BL2hFWVFPTnVSVk42VUl5c1Bob2RCVndsVTJEV1pYNndyZm5DZApNOFBCajNHSXVVMWVwV0RMUVZYa2cvdUxmZERaaU8xZ1p5UWhNN3V0ekE5WVMzK3VXaGtmczRUdFE1Q3ZVdEx1ClI0enlMVkQ3QWdNQkFBRUNnZ0VBYkd4a3EzeVZ5WVdoQU1aQjlhT2tXMUhKWE9iU3Z6UUJWbE1QZG9wZS9nRVYKSEFtWWExNk81Y0NFejNRUWpBWFJyMlA1bzZJTTZkOUVlMVRxRFRzQ0c5ZmEwYmxuT0tyMHdlaDA0dksyc010OApQV2RlVlNiTzZHZHIyTmRCdkREWEZxOEhURHdBc2dMaFVoNVRIZ2VQNmgvdjBkQTJkRXNMS0pHVTM4QUR1aG05ClpKRCthbm5KQ1BFVXNvMmtqeE40UjFHUmQwb2ZLSWFZeTg3dlhiK3FleXpsL1lreUEvYU9wMkh1S1RENmVpRGkKbTZrWUp6Q1k3ZEluYjlCNlZTTm52UlAzNyttM0JLSFlRaE5kbVJKQlp4c1pFQTRKaFYrQVYwNHlWUE1la3FoMgpqeVVxRFBEaGVMVW5FalJmc0FnOFVNU0JXQU5sLzkyNzNoR2FGeHQvTVFLQmdRRGVhNUhFd2oyVVBhcS9yMzdsCm90cFhBUU9qeEFLb2tyMWZNVTlaWksyTWdLY2hoSks0c1R0Uk5VbE03M2lDeHBPZTJTbW5ZeG5GcldrOVRkRjEKQ3habFJyVDBKZGxHOXJEMlNCSUR6b3FBaytWbHE5YzIxNE12NXdQZkpLZEpxaCtaVGZKdmZtNU9halhjeXFMZQpRSVRmdGVpdFRNWFJpQjBsaFpNSEo0RzVVd0tCZ1FEVS84OFo0UUxGa0xnWDMvMUMxcit4V3NuVUY5UGtHVXpSCm9USmg0enVXNHpwSkRDNitLS0lwckVZajIzdEdFUWxEZHJGcFBIWFZtMGVqV2QrRGN6THlmNFdZdWIxTTBuRksKbUpSaGhOMXhFRitNVmtPYjlWS2ZOU2xFUG16WHptcTlpdjVxUVlmZHJPLzhhQ0JMUW5UcUYveTJUUVVNN2tsNQpXbGo3Si9lTXVRS0JnUUNxTmJXNjFrN2JxQW1JWVp3QnpndTY4enErMDV5Wk5wcVhRNXdPcy80Zi9NQnA1Uk9ICkpaSllSaWdQS1YrVzdMSkJxTHk0clIwbTZ0c1RuLzYvekRsYVRhN2kvQ2YzcDRlcklXSXY2WnFTWlJ2ekgzczIKSzl6b0Jxa3UxZFR6aWE1ZTJvakNEQVlNR2ptWCtyYUMwT3NlYkE1Z3VOVFYwWTFFanFFQ281Z2hvd0tCZ1FDRQo5MnlCNjBXZnI4ZzhuMGVyQWdTSTR2UTd3dVF6OE5kVHhoMTluaTBFOUxUZUJRenBDTlN5enlpNkdibks4N2VrCnRlUHFuaU94UlU1ald5ZDlGOTBtSlJWeFVnSXFndlRXYkltMGx3em1HQ0tOcVF4cnY2bmtXWHQ1YnI3anVhaEkKeXd3bnFPRDRNWTFmTkdGMG1mZ0NheGNIZHUxQU5VRUkwSzNibkFlZGdRS0JnR1Y5VGxOSmtQZ2xma1A5dFRDTApldWtPVkNiTmNFc1dSRHYzMTFQa3pKemw3OHNRbENXL0NWK2NnK2JHOXB5ZEJJWUx3ZVRrUnp0d3FoVFE1L2JECmw1cWM1MVVKSkxJeEJ1TEIwZGJnMVh0eDF0VUdTaEV5UTFDc2U5SEwzKzgrbVRCN2drNDdpb2NzUEFuNXMxVnoKQ1ZjUVFQRnVmYWkrRzNwakI1Q0gvUEtnCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\n" +
			"        \"crt\": " +
					"\"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURzakNDQXBvQ0NRRHpibHduYzNBLzVqQU5CZ2txaGtpRzl3MEJBUXNGQURDQm1URUxNQWtHQTFVRUJoTUMKVlZNeEN6QUpCZ05WQkFnTUFrTlBNUXd3Q2dZRFZRUUhEQU5FUlU0eER6QU5CZ05WQkFvTUJrRndZV05vWlRFTApNQWtHQTFVRUN3d0NWRU14TGpBc0JnTlZCQU1NSlNvdWFIUjBjSE10ZG1Gc2FXUXRkR1Z6ZEM1MGFHVmpaRzR1ClpYaGhiWEJzWlM1amIyMHhJVEFmQmdrcWhraUc5dzBCQ1FFV0VuUmpRSE5sYkdZdGMybG5ibVZrTG1OdmJUQWcKRncweE9UQXpNRFV4TnpRMk1UTmFHQTh5TVRFNU1ESXdPVEUzTkRZeE0xb3dnWmt4Q3pBSkJnTlZCQVlUQWxWVApNUXN3Q1FZRFZRUUlEQUpEVHpFTU1Bb0dBMVVFQnd3RFJFVk9NUTh3RFFZRFZRUUtEQVpCY0dGamFHVXhDekFKCkJnTlZCQXNNQWxSRE1TNHdMQVlEVlFRRERDVXFMbWgwZEhCekxYWmhiR2xrTFhSbGMzUXVkR2hsWTJSdUxtVjQKWVcxd2JHVXVZMjl0TVNFd0h3WUpLb1pJaHZjTkFRa0JGaEowWTBCelpXeG1MWE5wWjI1bFpDNWpiMjB3Z2dFaQpNQTBHQ1NxR1NJYjNEUUVCQVFVQUE0SUJEd0F3Z2dFS0FvSUJBUUM1RDFYTW1yYkMvQk9YUkZMVkczbTNSbmhWCmdGSXVEOXV4VkhCQ2F0dkxBbnNmcmpYQjNrcmY1TUw1bkt3WUVpdzgrZEFqNjdWdkJEeHNDMTYvbFFBbFM4YnAKcU9TUW81OU9EQ3FQTWZmWmM1VWs3VXY1MzdEeTFhRzI0Yk9UdHlMY0MyMXNjMUptWFh1Y0VlQlZUZldFWUVLdQovTHBxL2Q2WVJTbGtZV1FLdkwwZlM0Y2tBbXFCZEVZNkNLN2o2cmJ6YWRiSFRwdkl3UFhoWVNTOWlJOFQxKzRTCmE2Qzk5Y3J0VEdmWW9tQS9oRVlRT051UlZONlVJeXNQaG9kQlZ3bFUyRFdaWDZ3cmZuQ2RNOFBCajNHSXVVMWUKcFdETFFWWGtnL3VMZmREWmlPMWdaeVFoTTd1dHpBOVlTMyt1V2hrZnM0VHRRNUN2VXRMdVI0enlMVkQ3QWdNQgpBQUV3RFFZSktvWklodmNOQVFFTEJRQURnZ0VCQUh0clNXMUE5em83K1NkTkM0RGVFeXdQOTZOeE95eDdobHNzCk9UVjhET1NZcE1yYUNkUGhVUHBWZmpLYmdnOVZTd0lmL0tOamRFZVNKS08vYXhqNVNNM3F5aE9obGUzdXdlMGEKWWlpa21saVU3Wkc5THhJTTFVZ0ZaN24wNURVV2MyN0RWTHYwUSttNVdGZ2VPSi82WklqTTg1RStYcDJ0c0svZwprNDVsbm1iMVNGb1l2LzJuWHEzdkJNaGJPUUVrNDY1WE5OZmRDU1c3RDhCcVJHYitoU1NmT0NHQVlDQkNxVjhKCnBNNHBadFhZZkczT0tCUmRXSTR6VU5JazdiU3lWQW1LWU5Pc3Frby9UMi90NmM0em1ZbU4wTjdMMVljOTk1algKLzBlbE51NW1vMjNOKzJwSzRuaFZjQXZ3VE5HdnhMbWdlcVV4cW44TVdPS29LVGdYNys0PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==\"\n"+
			"      },\n" +
			"      \"hostname\": \"*.https-valid-test.thecdn.example.com\"\n" +
		"    }";
	private final static String EXPIRED_CERT_DATA =
		"    {\n" +
			"      \"deliveryservice\": \"http-to-https-test\",\n" +
			"      \"certificate\": {\n" +
			"        \"comment\" : \"The following self signed certificate which expired on 3/5/2019 \",\n" +
			"        \"key\": " +
				"\"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUV2UUlCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktjd2dnU2pBZ0VBQW9JQkFRQ2paZ0xpTHNRS0o0UXgKS0F5WnFqT0NOL2lXUmgzdXkyZCtyd0J1VWJ2NTBIVEk0QUdnOHE2eG9pbzNtZHFHZXRXdVJIemUvSmQ5ckxJSQpvWDFXOGNFeTVybW0wV0xXYnJDbzlJQUE5K0dHcTgyNXUreGplemdiSHg3TEt1N3lqdUJVVjI4SyszTXNOQUhGCjZsemZNdTJ5VEFUMExvU25waDRWeHZLRDlzM05rNzdtaW5vcVR0aGFlSldxWXVEQlZ4WTBNS3JGbWJuQkEybkcKV295ZHIrelBROHB2N3Fka0FmcXlnZ0loWjloM0JBNnVESHRtcDNueWlsc2ZSMHpGeFR4QnRhaVhTbnRZdXM4NAphVUNwaHVjWkIvYW5tVHJvMHFSSG82czFhcWd1alJIK2xtWXNySjRjQ01QSmd4eG5YWTM0MUpDZm42T0N3c1FXClNhS3huaS9EQWdNQkFBRUNnZ0VBQytQamQ5UVJYZS9NTmN1RlJ6VlVkRGhnZFliNnJLTE9rREJwNXAwNkFZN0MKd005VUx3TVo1VUU0c3owVjRzMVRlVS93aWtWMVBLYnhlYUZPdnFIdS9pWStBajZnWTV4QWJMc0dDWXdBTkUyUwpOZDdQNzlsS2x1YW4xZjcwemwvSlFUbnZrYXdFa0lYa1R5T2p5SFlyUjlzeVRSYUpmcTJlNk5UR1Z3WUJxZURsCnJvTFByYkp4OXh4TXdPb2ZoN0tHSkZMQ0E4Q1JheGd2U3RJU2JpT1RGTWN5UThOcmhvNlNpOVJJVHNtT3kvTHcKVjF5d2JFaVpoOG5LNFdxd2pSYmR4NzJ5SWxMZ2RUeC90bkFiOUNLM1BqdkZyRTRZajFqbjc0NGF3NGZYUTMyOApiSC9uYWRENmlCeWQ0dmpLUjBLUWlRK0E4ZmViRW5EZzFSVW8rQndpWVFLQmdRRFRDbGQxV20vN3ZqdzFUMkx5Cnhlb3NFdlloYW1DclVRVjRyTCtRcmg0S0JTckVHVEZoZXMrclhBNUxEY29iS01hY1RwSS9LMXVVaHg5K3ZLV1kKVWk0blVKVjAyU283UnEwbW5tWVo2aXhLd0NQQWpESEwrQmxlcVgycFR1SU1hYUpWV2NleFI5dkNneTZVUmJLOQp4OUtWdzVqcE5HYVloK3RSRlV3cE5hR1Nzd0tCZ1FER05XT1d5MXJUdVNlM2hUTG0yakhTVGRlV0RSWlE5eEo3CkFQSmNtaW9xRHIxV1JVR3ZxV3Rkd1kySEl2blNzbDRSa2wxUU03bnpQNHNqN2ZSUjllY2dMcDRXcGFXMTVGKzgKOHlGMXBOcVFSWjZYNkd1Z3JZdUZlT1J5YnZTdGFYLzR6bklQUUpadmJVOVF3OUZnU051cFgwTXJPMCtJWDdXQwpRRWQvR3pJMnNRS0JnR3dVSU1RbDQ3RytOQ0Z0SFpTTlRTYnpNdi9iOWRQbXMzR2dycDZPdlMyT2hkOVZzNWRqCmlOVU9XUGVSQVU4MWE3bUM5NXpJUEtkdEovRUU5WjF6Z05WN2pIOEI5SUhVNlRvYzV0Y2d1VHd5K0Z4VXIrL3cKaURXVmdaaGlvSnVRd2FVS1RKMTYybzNjRnMreWZoNTVKbHl5aGkzd094YWtqUnZDVjNYSFZJN0hBb0dCQUp0SAowbGlkd2U5aS9CR1RnWmhIMG9aT3c1bmpjTnRIWlN3R1J0bHpVWnNYWncvQ1BENnhQTkw3d3JQZkc5Y01OQlFTCkZaYXluM2hKRE9tK0R3MXkxM3BuNnlRVTYraS9ISjM3My9lNWloMUMzWWRtNTRLKzB6Sml6cDR6L080cVc3NkIKaGV3YkRvQUhJLzlER2JJVUFqc0R6YXg5ejhZb0xSdjQzY3BmZFF4UkFvR0Fabm9iSnhJMWVybERsSHBrN0pibApUVnBhUWgvZVBNajgxYXQvNjcxdXRUb3RMdHlvTEZkRE5sb0JlNUVSc2luOXBjaHhSbkNNVVdsRzUxZjNaTmViCkNmbEtVN053UjNZVWRZYU9OVWdEZVp3RkhqOExYMGVPUVQrMENjamR2L2FoWVVIb2JLNWdvdkxXd1ljSHdIY2wKL09vTjhmODVBVDBIaTJ1VldwS2VrZmc9Ci0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\n" +
			"        \"crt\": " +
				"\"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURxakNDQXBJQ0NRQ0Q1OFZ3U2IrNWJqQU5CZ2txaGtpRzl3MEJBUXNGQURDQmxqRUxNQWtHQTFVRUJoTUMKVlZNeEN6QUpCZ05WQkFnTUFrTlBNUXd3Q2dZRFZRUUhEQU5FUlU0eER6QU5CZ05WQkFvTUJrRndZV05vWlRFTApNQWtHQTFVRUN3d0NWRU14TURBdUJnTlZCQU1NSnlvdWFIUjBjQzEwYnkxb2RIUndjeTEwWlhOMExuUm9aV05rCmJpNWxlR0Z0Y0d4bExtTnZiVEVjTUJvR0NTcUdTSWIzRFFFSkFSWU5kR05BWVhCaFkyaGxMbU52YlRBZUZ3MHgKT1RBek1EUXlNekl6TkRKYUZ3MHhPVEF6TURVeU16SXpOREphTUlHV01Rc3dDUVlEVlFRR0V3SlZVekVMTUFrRwpBMVVFQ0F3Q1EwOHhEREFLQmdOVkJBY01BMFJGVGpFUE1BMEdBMVVFQ2d3R1FYQmhZMmhsTVFzd0NRWURWUVFMCkRBSlVRekV3TUM0R0ExVUVBd3duS2k1b2RIUndMWFJ2TFdoMGRIQnpMWFJsYzNRdWRHaGxZMlJ1TG1WNFlXMXcKYkdVdVkyOXRNUnd3R2dZSktvWklodmNOQVFrQkZnMTBZMEJoY0dGamFHVXVZMjl0TUlJQklqQU5CZ2txaGtpRwo5dzBCQVFFRkFBT0NBUThBTUlJQkNnS0NBUUVBbzJZQzRpN0VDaWVFTVNnTW1hb3pnamY0bGtZZDdzdG5mcThBCmJsRzcrZEIweU9BQm9QS3VzYUlxTjVuYWhuclZya1I4M3Z5WGZheXlDS0Y5VnZIQk11YTVwdEZpMW02d3FQU0EKQVBmaGhxdk51YnZzWTNzNEd4OGV5eXJ1OG83Z1ZGZHZDdnR6TERRQnhlcGMzekx0c2t3RTlDNkVwNlllRmNieQpnL2JOelpPKzVvcDZLazdZV25pVnFtTGd3VmNXTkRDcXhabTV3UU5weGxxTW5hL3N6MFBLYis2blpBSDZzb0lDCklXZllkd1FPcmd4N1pxZDU4b3BiSDBkTXhjVThRYldvbDBwN1dMclBPR2xBcVlibkdRZjJwNWs2Nk5La1I2T3IKTldxb0xvMFIvcFptTEt5ZUhBakR5WU1jWjEyTitOU1FuNStqZ3NMRUZrbWlzWjR2d3dJREFRQUJNQTBHQ1NxRwpTSWIzRFFFQkN3VUFBNElCQVFCNUhCTFgvWU01QnQvTEVWUmFvazFBL1pSSzAvTmZjVzJLb0VQMk80VklvSEM0CnhqaGFzaERqWWdrME44d1NRTTd2UGxzR1NnUzZzSC9yM3NSeUt0bmZvNzFGMFh1K0lLSXV0Ylh2bmhjdXNXd0QKWXJUMExGaWQzUXl5TUNUTXRBMEpxTVdma3lIOWhlTk16cFI1blg3ODIyUFZzekhEUmpVZUhTSTZwbzB0TUNxZwpyVE10SHVSbVdJaGhhZzY1a29PMUNYTG81R3pkdGdmdTFwb2YzTnRWKzBqQWVidlFtUktqcWZBZUc3WXJTVEpwCk5yalVHdmZJMnpDZElDY1dIbUdTbndXNktSYXFOUFpoVHN2UWhyTEdMZDB0SU02MXZ0NjhPZWNFWXA0eWhlYnQKZFpjQmYxYkdMRWtiWlphTVVuaW9VZW1XSDJoYVVNcDdueWJxV2VQWQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==\"\n" +
			"      },\n" +
			"      \"hostname\": \"*.http-to-https-test.thecdn.example.com\"\n" +
		"    }";
	private final static String MOD_MISS_CERT_DATA =
		"    {\n" +
			"      \"deliveryservice\": \"https-mod-miss\",\n" +
			"      \"certificate\": {\n" +
			"        \"comment\" : \"The following certificate and key are for the same subject but have " +
				"mismatched modulus between the private and public keys\",\n" +
			"        \"key\": " +
				"\"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUV2QUlCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktZd2dnU2lBZ0VBQW9JQkFRQ3lIMW91SmpXcE5SeVAKdE1nVDM3emhCc0tYM1VhTWlhdm5CUHNJUDhidmdaUnNicFNYeVVSdTJsaWdsYWZpWlAybTZxZE1LZ3BHcUo4ZgpwZHQzODFsMHduakI5YURleU92NzZrcnJBdzhlME9VOW9ZU0Q1VVlWMU11M0I4ZTV1UGFLYkdNcDZvc3o1WDJSCmlDYnRjcTAvUzJaeWhWNXIvRkJqNUtsN2I0UlBNTjdPVXJNVW5LcHlXZ2hZNzdXOXpzVm96cTg5cldOL0g5VUEKZjFMRmFSdU1mckNvYVVKZHRKZEIyY2FGblkwWmI1MDhzcmFGWXplaFUvK3FjZE9heWtVa0hMMTVweDVFbS9mOQpYVlpNcmVJeFlobVdCK1I5MWJ5d3dsVmZCSm4zU3dIT3ZGMzVqSTh4d1dpMEx6V0x0Qk9pVm4yYlV3ZUN3dlVQCkdKb1VqbEJ0QWdNQkFBRUNnZ0VBRmpvWFZMN3IzMHVEWHVOZVBDeWxNeWRXelFDTnR5Zk96YXN2Y0I0VlF2blcKZlpsbTdYSHVHaThnOUJqNHRDV0tDWFFxb0RSMng4NXUzTklqaXRwUkJXTG5FcjBGOEFiK2U1Y0c5a0NSZUhUMAp4allMaFRIdEJ2aGcyMXdiTGkvSWhBbDJibHFZT0VlZzNiSXh1VnVnQnMvdjNzYUp1OHZtZERDcWZYNnk4ZmFmCnNFYmVTT2dkRFltRTg1SHZXUFFDTVVNV0MyZzJEekw2a0U1eCtxMmt5VkNlc1hSVTl6c3BtRkxsMGw5cndIN0sKbU9jMUVJZ3J0THExeUt6b3BKTUFTbVZPVmxTd1FUdGlOS3RGN3FZVXVudmpDdC8wWDM1clFIcDMwWXBCbVpHUApUQ0swMEFZMUZteDhUK2p5MENvdmVOWm9lM2JVTk5RM2l0MEhObWNWdVFLQmdRRHFiNVRFQmQ3aGw2cVRaS09xCkx3T1M5M2xJcU5nbFZnbzN2S09HUkF1MWpFYm9IaktDOTdudUdGSGNGdXZzTldISEpQaHRQUTJEcXUzREM1NHkKSXBOZ2Q4dVUwZWVBNjdxM2g2cWpJRjQ2c1pMZWdpaTh0aWlOZTJUK0dQVjBHYngyNU1XYjJtTVNXSjJUQU1UWAp3eHNEb1d5QkZEajU2MEJacXBqbzFTQTdkd0tCZ1FEQ2diaklUNWI0TWpFcHlyTEJKeUwvc1BjQzFUOWczUHdECjlvSGk0aXJFSWJzeDk3NTNhZnRpM3VOUlFZUHI1WW9qdkNQSHNibnQ2SXpaYUZaQUViem9Zc1ExSUJ3UkhDbVUKbWhiS0p2MDRhckVScTZtV1hIZng4MkszUC92a1VraFZSTzZHNzJ4eFB3UFJ6b3dteDZDbU9uUWVadkYrZTZNRwptc0xlQjVORU93S0JnQVp2YjY3OTFrTnREV0trWlpXN1dxYkRJbElyU0Z1bUEvdkpzdGR4c0x5WUVDNDQvZnY0Clh1TTVTYTMzOXh2eHp6QlBSSDZES1liT3YxNFdTSTVwd28vb1dlOUkzOGo3TDVId0tHLzM2SDVGOTVraUM0bzYKbWR4Z1ljSlQzeEVEejllWHFoRUFLcTRMUHJBVldsSHQ2aVRzWG5VZ24vdkVTR3p0c09yYlJ0bzdBb0dBR1VsVQpCSGFVWWQva2xGSk51dDZqcGlvVGNzTFdZbmxZS2d1NkJ3endFbDl3UHFhK2xEZXEvc2VMTmQwV2tXeGQ4UmRjCmIzR2pnbEpoUFVKYk5Da2FMZnZwRmg3K2h4cnFMTzk3VnZ5S252TC80aFEzRDkwbG1zYlJacEZpNWVQc2sybEsKdVRBWElRSFlOVVpzNGYzQjNOcHNqaWREN2ZXVTFCNzZobkxscWxFQ2dZQU5HOWZ2WFNhOWlnUC9acmdBQW55KwpLdlZUb1NOK0I3UDhGSUk4QW4zNTdzYXY3K0ZVWnZwZkQ1S1hkSHBZUEIxTk9tQVhGcE9FYjVvQTErZUNFRTh4Ck4wdktEaHoyK3VBSEFyczBud1ppUHBXbkZoaE5sLzJ5b1pUZHB0VWp1K0ZhSVg4RHFtSUovNGVMdVNERUR4MmUKcUVDTGt0SUxJWkk3eXVaNFIwNGFxZz09Ci0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\n" +
			"        \"crt\": " +
				"\"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURyRENDQXBRQ0NRQ0pZQWd0a1JtN3hUQU5CZ2txaGtpRzl3MEJBUXNGQURDQmxqRUxNQWtHQTFVRUJoTUMKVlZNeEN6QUpCZ05WQkFnTUFrTlBNUXd3Q2dZRFZRUUhEQU5FUlU0eER6QU5CZ05WQkFvTUJrRndZV05vWlRFTApNQWtHQTFVRUN3d0NWRU14S3pBcEJnTlZCQU1NSWlvdWFIUjBjQzF0YjJRdGJXbHpjeTUwYUdWalpHNHVaWGhoCmJYQnNaUzVqYjIweElUQWZCZ2txaGtpRzl3MEJDUUVXRW5SalFITmxiR1l0YzJsbmJtVmtMbU52YlRBZ0Z3MHgKT1RBek1EVXhPREE0TkRsYUdBOHlNVEU1TURJd09URTRNRGcwT1Zvd2daWXhDekFKQmdOVkJBWVRBbFZUTVFzdwpDUVlEVlFRSURBSkRUekVNTUFvR0ExVUVCd3dEUkVWT01ROHdEUVlEVlFRS0RBWkJjR0ZqYUdVeEN6QUpCZ05WCkJBc01BbFJETVNzd0tRWURWUVFERENJcUxtaDBkSEF0Ylc5a0xXMXBjM011ZEdobFkyUnVMbVY0WVcxd2JHVXUKWTI5dE1TRXdId1lKS29aSWh2Y05BUWtCRmhKMFkwQnpaV3htTFhOcFoyNWxaQzVqYjIwd2dnRWlNQTBHQ1NxRwpTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFEZ0Noa3J2SFMwK21uWUtUR1Y0NTl2N2ZURXA1ekVsMW4xClViQlI2aFNVbXZ6eUJYYWVpdU5wQ2NubUhSd01ma3c4dTNuNVVvMFhDOGY3Nllkc3gyRzlkL1h2dVdlMUFNdDQKWGF0SjVLa1AvNVNpVS9reWVhR3VwbmFTWWtvRHBKRlEzbTJ0cXhJeGh6ZklteUIzTFdJdEFUU3kzMGxtd0E5Ywp1cTRZaWNrSjVXUWxNYklNVW00OHlCcU5NOXlma1lRSy90WWwyb3l6Q1ZrN2s5MTdQdDFKS3JpcThrRTFKdDhlCkNqbUtxUXZMTjRLdzJ5dm9aNTZvTWtuaDlhRVAzRm55RDIzRWNtdGlXbWI0Q0hrSXowL20vczVGV0tLUDhzTHkKOHhCeUlUWjZPVUNzNEZUeVNiVGk3ODlGaVJ1WjljcE9rZVdDMFdQMkV0L1g5MmlZRnZIckFnTUJBQUV3RFFZSgpLb1pJaHZjTkFRRUxCUUFEZ2dFQkFITGJRczNrMnhpdVBpMjdWNVJnVFFHdytZb2YyNXQvcjBMVVVPOTE0TlZBClh1MjAyUDVMNy9wSTVFZTVLTi9ZVHQ2SnVoWWl2M0ZiQnZBd3FTaE43N2pHTGNFQi94VDhCVWxsbzBISzJteTYKR2YyL3BHZWxNL2syTFpxZEpMTXhCL2JsL3JGZUJCVHhWVkdJd3M4ZFZ3d1BuY0x0bnBEVmQzRnlOMGx5UzdMNQovVXQ2Wm00QTBlVEpxZUVlcnprRDZMQWVEY21vTXg1cGphZDNodldGTCsrdjdkazQ5T3QrSkNtSGx5ZTR6eEE1Cmc5RUQ0VXpaYnFZcmZ4Uk9lY25zTWJzUmllVlcyekh5dDQyVTVUSmtKK3JNdE9DempROUwrTGRDVTZhOENVVjUKWlBnYm1vMTJkN1NIUDBaY1FtOTg5THpEcXRRblMyRnJrS3pSdEh3ZHBjND0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo=\"\n"+
			"      },\n" +
			"      \"hostname\": \"*.http-mod-miss.thecdn.example.com\"\n" +
		"    }";

	@Before
	@PrepareForTest({Instant.class})
	public void before() throws Exception {
		Certificate certificate = new Certificate();
		certificate.setCrt("encodedchaindata");
		certificate.setKey("encodedkeydata");
		certificateData = new CertificateData();
		certificateData.setCertificate(certificate);
		certificateData.setDeliveryservice("some-delivery-service");
		certificateData.setHostname("example.com");
		certificateDataConverter = new CertificateDataConverter();
	}

	@Test
	public void itConvertsValidCertToHandshakeData() throws Exception {
		try {
			certificateData = ((CertificateData) new ObjectMapper().readValue(VALID_CERT_DATA,
					new TypeReference<CertificateData>() { }));
		} catch (Exception e) {
			fail("Failed parsing json data: " + e.getMessage());
		}
		HandshakeData handshakeData = certificateDataConverter.toHandshakeData(certificateData);
		assertThat(handshakeData, notNullValue());
		assertThat(handshakeData.getDeliveryService(), equalTo(certificateData.getDeliveryservice()));
		assertThat(handshakeData.getHostname(), equalTo(certificateData.getHostname()));
	}

	@Test
	public void itRejectsExpiredCert() throws Exception {
		try {
			certificateData = ((CertificateData) new ObjectMapper().readValue(EXPIRED_CERT_DATA,
					new TypeReference<CertificateData>() { }));
		} catch (Exception e) {
			fail("Failed parsing json data: " + e.getMessage());
		}
		HandshakeData handshakeData = certificateDataConverter.toHandshakeData(certificateData);
		assertThat(handshakeData, nullValue());
	}

	@Test
	public void itRejectsModulusMismatch() throws Exception {
		try {
			certificateData = ((CertificateData) new ObjectMapper().readValue(MOD_MISS_CERT_DATA,
					new TypeReference<CertificateData>() { }));
		} catch (Exception e) {
			fail("Failed parsing json data: " + e.getMessage());
		}
		HandshakeData handshakeData = certificateDataConverter.toHandshakeData(certificateData);
		assertThat(handshakeData, nullValue());
	}

	@Test
	public void itRejectsSubjectMismatch() throws Exception {
		try {
			certificateData = ((CertificateData) new ObjectMapper().readValue(SUBJECT_MISS_CERT_DATA,
					new TypeReference<CertificateData>() { }));
		} catch (Exception e) {
			fail("Failed parsing json data: " + e.getMessage());
		}
		HandshakeData handshakeData = certificateDataConverter.toHandshakeData(certificateData);
		assertThat(handshakeData, nullValue());
	}
}
