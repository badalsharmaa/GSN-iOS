package io.getsafenow.appdi.componentdi

import io.getsafenow.libraries.gsn_matrix.api.GsnMClient
import me.tatarka.inject.annotations.KmpComponentCreate

interface SessionComponentFactoryGsn {
    fun create(client: GsnMClient): SessionComponentGsn
}

interface RoomComponentFactoryGsn {
    fun create(args: RoomArgs): RoomComponentGsn
}

@KmpComponentCreate
expect fun createSessionComponent(
    parent: AppComponentGsn,
    client: GsnMClient
): SessionComponentGsn

@KmpComponentCreate
expect fun createRoomComponents(
    parent: SessionComponentGsn,
    args: RoomArgs
): RoomComponentGsn