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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import org.junit.rules.ExternalResource;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;

/**
 * Resource to load AWS credentials and get clients to desired services
 */
public class AwsCredentialResource extends ExternalResource {

  private static final String FAKE_KEY = "AKIAIZFKPYAKBFDZPAEA";
  private static final String FAKE_SECRET = "18S1bF4bpjCKZP2KRgbqOn7xJLDmqmwSXqq5GAWq";

  private AmazonS3Client client;
  private TransferManager tx;
  private ProfileCredentialsProvider provider;


  public AWSCredentialsProvider getProvider() {
    if (this.provider == null) {
      this.provider = new ProfileCredentialsProvider("aws-testing");
    }
    return this.provider;
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
}
