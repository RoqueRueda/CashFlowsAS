/*
 * Copyright 2014 Roque Rueda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.roque.rueda.cashflows.hepers;

/**
 * Input filter used to check the input value on a text edit view.
 *
 * @author Roque Rueda
 * @since 04/10/2014
 * @version 1.0
 *
 */
public interface FragmentDataNotifier {

    /**
     * Notify the implementation when the data needs to be refresh.
     */
    void notifyDataRefresh();

}
