/*
 *    Copyright 2015 Rui Gil
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */


package io.oceanos.shaderbox.opengl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompileResult {
    boolean isSuccess = true;
    // ERROR: 0:35: Too many arguments to constructor of 'vec3'
    Pattern errorPattern = Pattern.compile("ERROR:\\s(\\d+):(\\d+):(.*)");
    String error = "";
    int errorLine = 0;

    public CompileResult(boolean result, String error) {
        this.isSuccess = result;
        Matcher matcher = errorPattern.matcher(error);
        if (matcher.find()) {
            this.errorLine = Integer.parseInt(matcher.group(2))-1;
            this.error = matcher.group(3);
        } else errorLine = 0;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getError() {
        return error;
    }

    public int getErrorLine() {
        return this.errorLine;
    }

}
