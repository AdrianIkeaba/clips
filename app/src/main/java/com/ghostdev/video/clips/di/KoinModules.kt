package com.ghostdev.video.clips.di

import android.content.Context
import com.ghostdev.video.clips.ui.presentation.home.HomeLogic
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import com.ghostdev.video.clips.ui.presentation.login.LoginLogic
import com.ghostdev.video.clips.ui.presentation.profile.ProfileLogic
import com.ghostdev.video.clips.ui.presentation.register.RegisterLogic
import com.ghostdev.video.clips.ui.presentation.uploadandpost.UploadLogic
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module


val provideViewmodelModule = module {
    factoryOf(::LoginLogic)
    factoryOf(::RegisterLogic)
    factoryOf(::UploadLogic)
    factoryOf(::ProfileLogic)
    factoryOf(::HomeLogic)
}
val provideHttpClientModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(json = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }, contentType = ContentType.Any)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15000
                connectTimeoutMillis = 15000
                socketTimeoutMillis = 15000
            }
        }
    }
}


fun initKoin(context: Context) {
    startKoin {
        androidContext(context)
        modules(
            provideViewmodelModule,
            provideHttpClientModule
        )
    }
}