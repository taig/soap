package io.taig.android.soap.ops

import io.taig.android.soap.Reader

class reader[C]( container: C ) {
    def read[V]( key: String )( implicit r: Reader[C, V] ): Option[V] = r.read( container, key )
}