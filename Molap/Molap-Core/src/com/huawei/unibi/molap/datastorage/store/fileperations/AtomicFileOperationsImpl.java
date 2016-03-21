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

package com.huawei.unibi.molap.datastorage.store.fileperations;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.huawei.unibi.molap.constants.MolapCommonConstants;
import com.huawei.unibi.molap.datastorage.store.filesystem.MolapFile;
import com.huawei.unibi.molap.datastorage.store.impl.FileFactory;
import com.huawei.unibi.molap.datastorage.store.impl.FileFactory.FileType;

public class AtomicFileOperationsImpl implements AtomicFileOperations {

    private String filePath;

    private FileType fileType;

    private String tempWriteFilePath;

    private DataOutputStream dataOutStream;

    public AtomicFileOperationsImpl(String filePath, FileType fileType) {
        this.filePath = filePath;

        this.fileType = fileType;
    }

    @Override public DataInputStream openForRead() throws IOException {
        return FileFactory.getDataInputStream(filePath, fileType);
    }

    @Override public DataOutputStream openForWrite(FileWriteOperation operation)
            throws IOException {

        filePath = filePath.replace("\\", "/");

        tempWriteFilePath = filePath + MolapCommonConstants.TEMPWRITEFILEEXTENSION;

        if (FileFactory.isFileExist(tempWriteFilePath, fileType)) {
            FileFactory.getMolapFile(tempWriteFilePath, fileType).delete();
        }

        FileFactory.createNewFile(tempWriteFilePath, fileType);

        dataOutStream = FileFactory.getDataOutputStream(tempWriteFilePath, fileType);

        return dataOutStream;

    }

    /* (non-Javadoc)
     * @see com.huawei.unibi.molap.datastorage.store.fileperations.AtomicFileOperations#close()
     */
    @Override public void close() throws IOException {

        if (null != dataOutStream) {
            dataOutStream.close();

            MolapFile tempFile = FileFactory.getMolapFile(tempWriteFilePath, fileType);

            if (!tempFile.renameForce(filePath)) {
                throw new IOException("temporary file renaming failed");
            }
        }

    }

}
