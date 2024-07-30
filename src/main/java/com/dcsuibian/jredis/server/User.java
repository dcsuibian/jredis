package com.dcsuibian.jredis.server;

import com.dcsuibian.jredis.datastructure.Sds;

import java.util.List;

public class User {
    private Sds name;
    private int flags;
    private List<Sds> passwords;
    private List<Sds> selectors;
}
