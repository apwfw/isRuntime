package de.intarsys.tools.jaxb;

import de.intarsys.tools.hex.HexTools;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Convert a hex string notation to a String (UTF-8)
 */
public class HexStringAdapter extends XmlAdapter<String, String> {

  @Override
  public String marshal(String v) throws Exception {
    return HexTools.bytesToHexString(v.getBytes("UTF-8"));
  }

  @Override
  public String unmarshal(String v) throws Exception {
    return new String(HexTools.hexStringToBytes(v), "UTF-8");
  }

}
