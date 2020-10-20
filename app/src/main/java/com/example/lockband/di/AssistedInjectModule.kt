package com.example.lockband.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import com.squareup.inject.assisted.dagger2.AssistedModule

@InstallIn(FragmentComponent::class)
@AssistedModule
@Module()
interface AssistedInjectModule {
}