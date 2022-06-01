/*
 * Copyright 2020 gRPC authors.
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

package io.grpc.kotlin

import kotlinx.coroutines.CopyableThreadContextElement
import kotlin.coroutines.CoroutineContext
import io.grpc.Context as GrpcContext

/**
 * A [CoroutineContext] that propagates an associated [io.grpc.Context] to coroutines run using
 * that context, regardless of thread.
 */
class GrpcContextElement(private val grpcContext: GrpcContext) : CopyableThreadContextElement<GrpcContext> {
  companion object Key : CoroutineContext.Key<GrpcContextElement> {
    fun current(): GrpcContextElement = GrpcContextElement(GrpcContext.current())
  }

  override val key: CoroutineContext.Key<GrpcContextElement>
    get() = Key

  override fun restoreThreadContext(context: CoroutineContext, oldState: GrpcContext) {
    grpcContext.detach(oldState)
  }

  override fun updateThreadContext(context: CoroutineContext): GrpcContext {
    return grpcContext.attach()
  }

  override fun copyForChild(): GrpcContextElement {
    // Copy from the ThreadLocal source of truth at child coroutine launch time. This makes
    // ThreadLocal writes between resumption of the parent coroutine and the launch of the
    // child coroutine visible to the child.
    val curr = GrpcContext.current()
    return GrpcContextElement(curr)
  }

  override fun mergeForChild(overwritingElement: CoroutineContext.Element): CoroutineContext {
    // Merge operation defines how to handle situations when both
    // the parent coroutine has an element in the context and
    // an element with the same key was also
    // explicitly passed to the child coroutine.
    // If merging does not require special behavior,
    // the copy of the element can be returned.
    return GrpcContextElement(GrpcContext.current())
  }
}
