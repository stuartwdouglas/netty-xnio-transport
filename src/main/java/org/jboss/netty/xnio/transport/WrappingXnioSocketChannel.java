/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */
package org.jboss.netty.xnio.transport;

import io.netty.channel.ChannelPromise;
import org.xnio.Option;
import org.xnio.StreamConnection;
import org.xnio.XnioIoThread;
import org.xnio.channels.AcceptingChannel;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * {@link AbstractXnioSocketChannel} implementation which allows you to wrap a pre-created XNIO channel.
 *
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
public final class WrappingXnioSocketChannel extends AbstractXnioSocketChannel {
    private final StreamConnection channel;
    final XnioIoThread thread;

    WrappingXnioSocketChannel(AbstractXnioServerSocketChannel parent, StreamConnection channel) {
        super(parent);
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        this.channel = channel;
        this.thread = channel.getIoThread();
        config().setTcpNoDelay(true);
        channel.getSourceChannel().getReadSetter().set(new ReadListener());
    }

    /**
     * Create a new {@link WrappingXnioSocketChannel} which was created via the given {@link AcceptingChannel} and uses
     * the given {@link StreamConnection} under the covers.
     */
    public WrappingXnioSocketChannel(AcceptingChannel<StreamConnection> parent, StreamConnection channel) {
        this(new WrappingXnioServerSocketChannel(parent), channel);
    }

    /**
     * Create a {@link WrappingXnioSocketChannel} which uses the given {@link StreamConnection} under the covers.
     */
    public WrappingXnioSocketChannel(StreamConnection channel) {
        this((AbstractXnioServerSocketChannel) null, channel);
    }

    @Override
    protected XnioUnsafe newUnsafe() {
        return new XnioUnsafe();
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        throw XnioUtils.unsupportedForWrapped();
    }

    private final class XnioUnsafe extends AbstractXnioUnsafe {
        @Override
        public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            promise.setFailure(XnioUtils.unsupportedForWrapped());
        }
    }

    @Override
    protected <T> void setOption0(Option<T> option, T value) throws IOException {
        channel.setOption(option, value);
    }

    @Override
    protected <T> T getOption0(Option<T> option) throws IOException {
        return channel.getOption(option);
    }

    @Override
    protected StreamConnection connection() {
        return channel;
    }
}
