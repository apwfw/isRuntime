package de.intarsys.tools.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.sql.Timestamp;

/**
 * Marshalling java timestamps.
 */
public class TimestampAdapter extends XmlAdapter<Long, Timestamp> {

  @Override
  public Long marshal(Timestamp v) throws Exception {
    return v.getTime();
  }

  @Override
  public Timestamp unmarshal(Long v) throws Exception {
    return new Timestamp(v);
  }

}
