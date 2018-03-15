/*
 * Copyright (c) Tuenti Technologies S.L. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.get_slyncy.slyncy.View.Test;

/**
 * The SmsStorage has the responsibility to store the last sms intercepted by the library.
 *
 * @author Pedro Vicente Gómez Sánchez <pgomez@tuenti.com>
 * @author Manuel Peinado <mpeinado@tuenti.com>
 */
interface MmsStorage
{
    void updateLastMmsIntercepted(int mmsId);

    int getLastMmsIntercepted();

    boolean isFirstMmsIntercepted();

    void addNewMessage(int mmsId, boolean isRead);

    boolean isUnread(int mmsId);

    void removeMessage(int mmsId);

    void markRead(int smsId);

    boolean isAdded(int mmsId);
}
