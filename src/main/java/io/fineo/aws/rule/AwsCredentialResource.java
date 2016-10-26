/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fineo.aws.rule;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Resource to load AWS credentials and get clients to desired services
 */
public class AwsCredentialResource extends ExternalResource {

  private static final Logger LOG = LoggerFactory.getLogger(AwsCredentialResource.class);
  private String CREDENTIALS = "credentials";

  private static final String FAKE_KEY = "AKIAIZFKPYAKBFDZPAEA";
  private static final String FAKE_SECRET = "18S1bF4bpjCKZP2KRgbqOn7xJLDmqmwSXqq5GAWq";

  private AmazonS3Client client;
  private TransferManager tx;
  private AWSCredentialsProvider provider;


  public AWSCredentialsProvider getProvider() {
    if (this.provider == null) {
      this.provider = new AWSCredentialsProviderChain(getSpecifiedFileCredentials(),
        new ProfileCredentialsProvider("aws-testing"),
        new EnvironmentVariableCredentialsProvider());
    }
    return this.provider;
  }

  private AWSCredentialsProvider getSpecifiedFileCredentials() {
    String credentialsFile = System.getProperty(CREDENTIALS);
    String key = null;
    String secret = null;
    if (credentialsFile != null) {
      try {
        FileInputStream is = new FileInputStream(credentialsFile);
        Yaml yaml = new Yaml();
        Map<String, String> map = (Map) yaml.load(is);
        key = map.get("access_key_id");
        secret = map.get("secrect_access_key");
      } catch (FileNotFoundException e) {
        LOG.warn("Invalide credentials file: " + credentialsFile + "! Skipping test. Specify a "
                 + "credentials file with: -D" + CREDENTIALS);
      }
    } else {
      LOG.warn("Not credentials file set! Specify a credentials file with: -D" + CREDENTIALS);
    }
    return new StaticCredentialsProvider(new EmptyAllowedCredentials(key, secret));
  }

  /**
   * Get a provider with the credentials stored under "aws-fake". This allows you configure a set
   * of 'credentials' but not have them actually point at any real user. This is useful for cases
   * of local testing, e.g. DynamoDBLocal
   *
   * @return a credentials provider with real looking credentials
   */
  public AWSCredentialsProvider getFakeProvider() {
    return new StaticCredentialsProvider(new BasicAWSCredentials(FAKE_KEY, FAKE_SECRET));
  }

  public AmazonS3Client getClient() {
    if (this.client == null) {
      ClientConfiguration config = new ClientConfiguration();
      client = new AmazonS3Client(getProvider().getCredentials(), config);
    }

    return this.client;
  }

  public TransferManager getTransferManager() {
    if (this.tx == null) {
      tx = new TransferManager(getClient());
    }
    return this.tx;
  }

  private class EmptyAllowedCredentials implements AWSCredentials{

    private final String key, secret;

    public EmptyAllowedCredentials(String key, String secret) {
      this.key = key;
      this.secret = secret;
    }

    @Override
    public String getAWSAccessKeyId() {
      return key;
    }

    @Override
    public String getAWSSecretKey() {
      return secret;
    }
  }
}
