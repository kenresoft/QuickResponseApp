/*
 * Copyright 2021 LiteKite Startup. All rights reserved.
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
package com.kixfobby.security.quickresponse.startup;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.startup.Initializer;
import com.kixfobby.security.quickresponse.room.database.AppDatabase;
import java.util.ArrayList;
import java.util.List;

/**
 * Initializes {@link AppDatabase} during app start-up by AppStartup Library
 *
 * @author Vignesh S
 * @version 1.0, 29/03/2021
 * @since 1.0
 */
public class AppDatabaseInitializer implements Initializer<AppDatabase> {

    @NonNull
    @Override
    public AppDatabase create(@NonNull Context context) {
        return InitializerEntryPoint.getEntryPoint(context).getAppDatabase();
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return new ArrayList<>();
    }
}
