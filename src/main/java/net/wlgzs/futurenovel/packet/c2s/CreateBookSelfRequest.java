package net.wlgzs.futurenovel.packet.c2s;

import javax.validation.constraints.Size;

public class CreateBookSelfRequest {
    @Size(min = 1)
    public String title;
}
