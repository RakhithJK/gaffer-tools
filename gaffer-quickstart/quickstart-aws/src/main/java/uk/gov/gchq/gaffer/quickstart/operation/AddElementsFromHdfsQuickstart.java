/*
 * Copyright 2016-2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.gaffer.quickstart.operation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.exception.CloneFailedException;
import uk.gov.gchq.gaffer.commonutil.Required;
import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.Validatable;

import java.util.Map;

public class AddElementsFromHdfsQuickstart implements Operation, Validatable {

    @Required
    private String dataPath;
    @Required
    private String elementGeneratorConfig;

    private String outputPath;
    private String failurePath;
    private int numPartitions = 0;

    private boolean validate = true;

    public void setDataPath(final String inputPath) {
        this.dataPath = inputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getFailurePath() {
        return failurePath;
    }

    public void setFailurePath(String failurePath) {
        this.failurePath = failurePath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setElementGeneratorConfig(final String elementGeneratorConfigPath) {
        this.elementGeneratorConfig = elementGeneratorConfigPath;
    }

    public int getNumPartitions() {
        return numPartitions;
    }

    public void setNumPartitions(int numPartitions) {
        this.numPartitions = numPartitions;
    }

    public String getDataPath(){
        return dataPath;
    }

    public String getElementGeneratorConfig(){
        return this.elementGeneratorConfig;
    }

    @Override
    public Operation shallowClone() throws CloneFailedException {
        return new Builder()
                .dataPath(dataPath)
                .outputPath(outputPath)
                .failurePath(failurePath)
                .elementGeneratorConfig(elementGeneratorConfig)
                .validate(validate)
                .numPartitions(numPartitions)
                .build();
    }

    @JsonIgnore
    @Override
    public Map<String, String> getOptions() {
        return null;
    }

    @JsonIgnore
    @Override
    public void setOptions(Map<String, String> options) {

    }

    @Override
    public boolean isSkipInvalidElements() {
        return false;
    }

    @Override
    public void setSkipInvalidElements(boolean skipInvalidElements) {

    }

    @Override
    public boolean isValidate() {
        return validate;
    }

    @Override
    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public static class Builder extends BaseBuilder<AddElementsFromHdfsQuickstart, Builder> implements Validatable.Builder<AddElementsFromHdfsQuickstart, Builder> {
        public Builder() {
            super(new AddElementsFromHdfsQuickstart());
        }

        public Builder dataPath(final String inputPath){
            _getOp().setDataPath(inputPath);
            return _self();
        }

        public Builder outputPath(final String outputPath){
            _getOp().setOutputPath(outputPath);
            return _self();
        }

        public Builder failurePath(final String failurePath){
            _getOp().setFailurePath(failurePath);
            return _self();
        }

        public Builder validate(final boolean validate){
            _getOp().setValidate(validate);
            return _self();
        }

        public Builder numPartitions(final int numPartitions){
            _getOp().setNumPartitions(numPartitions);
            return _self();
        }

        public Builder elementGeneratorConfig(final String elementGeneratorConfigPath){
                _getOp().setElementGeneratorConfig(elementGeneratorConfigPath);

            return _self();
        }
    }
}




