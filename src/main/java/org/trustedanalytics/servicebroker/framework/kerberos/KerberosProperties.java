/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trustedanalytics.servicebroker.framework.kerberos;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public final class KerberosProperties {

  private final static String KDC_PROPERTY = "kdc";
  private final static String REALM_PROPERTY = "krealm";
  private final static String CERT_PROPERTY = "cacert";

  private final String kdc;
  private final String realm;
  private final String cacert;
  private final Map<String, Object> credentials;
  private final boolean isKerberosEnabled;

  public KerberosProperties(String kdc, String realm, String cacert, boolean isKerberosEnabled) {
    this.kdc = kdc;
    this.realm = realm;
    this.cacert = cacert;
    this.isKerberosEnabled = isKerberosEnabled;
    this.credentials =
        ImmutableMap.of(KDC_PROPERTY, kdc, REALM_PROPERTY, realm, CERT_PROPERTY, cacert);
  }

  public String getKdc() {
    return kdc;
  }

  public String getRealm() {
    return realm;
  }

  public Map<String, Object> getCredentials() {
    return credentials;
  }

  public boolean isKerberosEnabled() {
    return isKerberosEnabled;
  }
}
