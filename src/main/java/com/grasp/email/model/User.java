package com.grasp.email.model;


import lombok.Data;

import java.math.BigInteger;

@Data
public class User {
    private BigInteger id;
    private String forename;
    private String surname;


}
