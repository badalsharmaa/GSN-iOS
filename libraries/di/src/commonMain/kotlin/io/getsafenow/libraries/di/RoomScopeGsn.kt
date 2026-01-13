package io.getsafenow.libraries.di

import me.tatarka.inject.annotations.Scope

/**
 * Scope annotation for room-level components.
 *
 * Use this to scope dependencies to the lifetime of a single chat room.
 */
abstract class RoomScopeGsn private constructor()