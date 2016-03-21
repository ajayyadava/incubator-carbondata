/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.huawei.unibi.molap.datastorage.store.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.huawei.iweb.platform.logging.LogService;
import com.huawei.iweb.platform.logging.LogServiceFactory;
import com.huawei.unibi.molap.constants.MolapCommonConstants;
import com.huawei.unibi.molap.datastorage.store.FileHolder;
import com.huawei.unibi.molap.util.MolapCoreLogEvent;

public class FileHolderImpl implements FileHolder {
    /**
     * Attribute for Molap LOGGER
     */
    private static final LogService LOGGER =
            LogServiceFactory.getLogService(FileHolderImpl.class.getName());
    /**
     * cache to hold filename and its stream
     */
    private Map<String, FileChannel> fileNameAndStreamCache;

    /**
     * FileHolderImpl Constructor
     * It will create the cache
     */
    public FileHolderImpl() {
        this.fileNameAndStreamCache =
                new HashMap<String, FileChannel>(MolapCommonConstants.DEFAULT_COLLECTION_SIZE);
    }

    public FileHolderImpl(int capacity) {
        this.fileNameAndStreamCache = new HashMap<String, FileChannel>(capacity);
    }

    /**
     * This method will be used to read the byte array from file based on offset and length(number of bytes) need to read
     *
     * @param filePath fully qualified file path
     * @param offset   reading start position,
     * @param length   number of bytes to be read
     * @return read byte array
     */
    @Override public byte[] readByteArray(String filePath, long offset, int length) {
        FileChannel fileChannel = updateCache(filePath);
        ByteBuffer byteBffer = read(fileChannel, length, offset);
        return byteBffer.array();
    }

    /**
     * This method will be used to read the bytebuffer from file based on offset and length(number of bytes) need to read
     *
     * @param filePath fully qualified file path
     * @param offset   reading start position,
     * @param length   number of bytes to be read
     * @return read byte array
     */
    @Override public ByteBuffer readByteBuffer(String filePath, long offset, int length) {
        FileChannel fileChannel = updateCache(filePath);
        ByteBuffer byteBffer = read(fileChannel, length, offset);
        return byteBffer;
    }

    /**
     * This method will be used to close all the streams currently present in the cache
     */
    @Override public void finish() {

        for (Entry<String, FileChannel> entry : fileNameAndStreamCache.entrySet()) {
            try {
                FileChannel channel = entry.getValue();
                if (null != channel) {
                    channel.close();
                }
            } catch (IOException exception) {
                LOGGER.error(MolapCoreLogEvent.UNIBI_MOLAPCORE_MSG, exception,
                        exception.getMessage());
            }
        }

    }

    /**
     * This method will be used to read int from file from postion(offset), here
     * length will be always 4 bacause int byte size if 4
     *
     * @param filePath fully qualified file path
     * @param offset   reading start position,
     * @return read int
     */
    @Override public int readInt(String filePath, long offset) {
        FileChannel fileChannel = updateCache(filePath);
        ByteBuffer byteBffer = read(fileChannel, MolapCommonConstants.INT_SIZE_IN_BYTE, offset);
        return byteBffer.getInt();
    }

    /**
     * This method will be used to read int from file from postion(offset), here
     * length will be always 4 bacause int byte size if 4
     *
     * @param filePath fully qualified file path
     * @return read int
     */
    @Override public int readInt(String filePath) {
        FileChannel fileChannel = updateCache(filePath);
        ByteBuffer byteBffer = read(fileChannel, MolapCommonConstants.INT_SIZE_IN_BYTE);
        return byteBffer.getInt();
    }

    /**
     * This method will be used to read int from file from postion(offset), here
     * length will be always 4 bacause int byte size if 4
     *
     * @param filePath fully qualified file path
     * @param offset   reading start position,
     * @return read int
     */
    @Override public long readDouble(String filePath, long offset) {
        FileChannel fileChannel = updateCache(filePath);
        ByteBuffer byteBffer = read(fileChannel, MolapCommonConstants.LONG_SIZE_IN_BYTE, offset);
        return byteBffer.getLong();
    }

    /**
     * This method will be used to check whether stream is already present in
     * cache or not for filepath if not present then create it and then add to
     * cache, other wise get from cache
     *
     * @param filePath fully qualified file path
     * @return channel
     */
    private FileChannel updateCache(String filePath) {
        FileChannel fileChannel = fileNameAndStreamCache.get(filePath);
        try {
            if (null == fileChannel) {
                FileInputStream stream = new FileInputStream(filePath);
                fileChannel = stream.getChannel();
                fileNameAndStreamCache.put(filePath, fileChannel);
            }
        } catch (IOException e) {
            LOGGER.error(MolapCoreLogEvent.UNIBI_MOLAPCORE_MSG, e, e.getMessage());
        }
        return fileChannel;
    }

    /**
     * This method will be used to read from file based on number of bytes to be read and positon
     *
     * @param channel file channel
     * @param size    number of bytes
     * @param offset  position
     * @return byte buffer
     */
    private ByteBuffer read(FileChannel channel, int size, long offset) {
        ByteBuffer byteBffer = ByteBuffer.allocate(size);
        try {
            channel.position(offset);
            channel.read(byteBffer);
        } catch (Exception e) {
            LOGGER.error(MolapCoreLogEvent.UNIBI_MOLAPCORE_MSG, e, e.getMessage());
        }
        byteBffer.rewind();
        return byteBffer;
    }

    /**
     * This method will be used to read from file based on number of bytes to be read and positon
     *
     * @param channel file channel
     * @param size    number of bytes
     * @return byte buffer
     */
    private ByteBuffer read(FileChannel channel, int size) {
        ByteBuffer byteBffer = ByteBuffer.allocate(size);
        try {
            channel.read(byteBffer);
        } catch (Exception e) {
            LOGGER.error(MolapCoreLogEvent.UNIBI_MOLAPCORE_MSG, e, e.getMessage());
        }
        byteBffer.rewind();
        return byteBffer;
    }

    /**
     * @see com.huawei.unibi.molap.datastorage.store.FileHolder#getFileSize(java.lang.String)
     */
    @Override public long getFileSize(String filePath) {
        long size = 0;

        try {
            FileChannel fileChannel = updateCache(filePath);
            size = fileChannel.size();
        } catch (IOException e) {
            LOGGER.error(MolapCoreLogEvent.UNIBI_MOLAPCORE_MSG, e, e.getMessage());
        }
        return size;
    }

    /**
     * This method will be used to read the byte array from file based on length(number of bytes)
     *
     * @param filePath fully qualified file path
     * @param length   number of bytes to be read
     * @return read byte array
     */
    @Override public byte[] readByteArray(String filePath, int length) {
        FileChannel fileChannel = updateCache(filePath);
        ByteBuffer byteBffer = read(fileChannel, length);
        return byteBffer.array();
    }

    /**
     * This method will be used to read long from file from postion(offset), here
     * length will be always 8 bacause int byte size is 8
     *
     * @param filePath fully qualified file path
     * @param offset   reading start position,
     * @return read long
     */
    @Override public long readLong(String filePath, long offset) {
        FileChannel fileChannel = updateCache(filePath);
        ByteBuffer byteBffer = read(fileChannel, MolapCommonConstants.LONG_SIZE_IN_BYTE, offset);
        return byteBffer.getLong();
    }

}
