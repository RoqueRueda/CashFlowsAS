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
package com.roque.rueda.cashflows.model;

import java.util.Date;

/**
 * Model that contains all values for a cash movement.
 *
 * @author Roque Rueda
 * @since 26/06/2014
 * @version 1.0
 *
 */
public class Movement {

    /**
     * Id of the movement.
     */
    public long id;

    /**
     * Amount of money that will be used on this movement.
     */
    public double amount;

    /**
     * Description for this movement.
     */
    public String description;

    /**
     * Date of the movement.
     */
    public Date date;

    /**
     * Sing of the movement ("+" or "-").
     */
    public String sing;

    /**
     * Account to witch this movement belongs.
     */
    public long idAccount;

}
