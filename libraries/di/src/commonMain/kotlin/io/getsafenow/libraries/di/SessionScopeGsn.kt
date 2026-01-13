package io.getsafenow.libraries.di


import me.tatarka.inject.annotations.Scope

/**
 * Scope annotation for session-level components (e.g., a logged-in client session).
 *
 * Dependencies annotated/bound in this scope live for the lifetime of an active session
 * and are disposed when the client logs out.
 */
abstract class SessionScopeGsn private constructor()