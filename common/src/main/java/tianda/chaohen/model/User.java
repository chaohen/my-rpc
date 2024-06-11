package tianda.chaohen.model;

import jdk.jfr.DataAmount;
import lombok.Data;

import java.util.Date;

import java.io.Serializable;
import java.sql.Time;

@Data
public class User implements Serializable {
    private String name;

    private Date Time = new Date();
}
