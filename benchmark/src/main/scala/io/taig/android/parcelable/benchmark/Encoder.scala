package io.taig.android.parcelable.benchmark

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._

class Encoder {
    @Benchmark
    def add = 9 + 9

    @Benchmark
    def multiply = 9 * 9
}