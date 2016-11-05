/*
 * Copyright (c) 2007, intarsys consulting GmbH
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of intarsys nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without specific
 *   prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package de.intarsys.tools.monitor;

import de.intarsys.tools.dom.ElementConfigurationException;
import de.intarsys.tools.dom.ElementTools;
import de.intarsys.tools.format.TrivialIntegerFormat;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * todo 1 nested traces todo 1 refactor "sample" changes in TimeMonitor todo 1
 * nicht matchende samples unterst�tzen
 */
public abstract class Monitor extends AbstractMonitor {
  /**
   * The collected attributes
   */
  public static final String ATTR_NAME = "name";
  public static final String ATTR_MAX = "max";
  public static final String ATTR_MIN = "min";
  public static final String ATTR_COUNT = "count";
  public static final String ATTR_TOTAL = "total";
  public static final String ATTR_EFFECTIVE = "effective";
  public static final String ATTR_AVG = "avg";
  public static final String ATTR_FIRST = "first";
  public static final String ATTR_LAST = "last";
  public static final String ATTR_MAXACTIVE = "maxactive";
  protected static final String TXT_VER_SEPARATOR = " | ";
  protected static final String TXT_HOR_SEPARATOR = "------------------------------------------------------------------------";
  protected static final int COLWIDTH_LABEL = 80;
  protected static final int COLWIDTH_NUMBER = 7;
  /**
   * The default format to be used to format sample values of this monitor
   */
  private static Format DEFAULT_FORMAT = TrivialIntegerFormat.getInstance();
  /**
   * The value of the first sample.
   */
  protected volatile long first;

  /** attributes for the monitor statistics */
  /**
   * The value of the last sample so far.
   */
  protected volatile long last;
  /**
   * The number of active traces
   */
  protected volatile int active;
  /**
   * The maximum number of concurrent traces
   */
  protected int maxActive;
  protected MonitorStatistic statistic = new MonitorStatistic("stop",
      Integer.MAX_VALUE);
  /**
   * The collection of all sample statistic, keyed by description
   */
  protected Map sampleStatistics = new HashMap();
  /**
   * Flag if we should present absolute values or calculate relative to start
   */
  private boolean relative = true;

  public Monitor() {
    super();
  }

  /**
   * Create a Monitor.
   *
   * @param name The monitors name
   */
  public Monitor(String name) {
    super(name);
  }

  @Override
  public void configure(Element element) throws ElementConfigurationException {
    super.configure(element);
    if (ElementTools.getPathBoolean(element, "deltasamples", true)) {
      setRelative(true);
    }
  }

  /**
   * Perform the neccessary calculations at the end of a MonitorTrace.
   * <p>
   * <p>
   * Call this from a synchronized environment only!
   * </p>
   *
   * @param trace The trace to be considered in the calculation.
   */
  protected abstract void doCalculation(MonitorTrace trace);

  /*
   * (non-Javadoc)
   *
   * @see de.intarsys.tools.monitor.IMonitor#getActive()
   */
  public int getActive() {
    return active;
  }

  /*
   * The statistic attributes collected. For a list of returned attributes see
   * the static declarations. The map should not be stored and shared to avoid
   * inconsistencies for clients. <p> The method should be synchronized, even
   * so this is not critical if you are ok with data that may be not accurate
   * (but is "statistically"). Anyway, this method is not called often...
   *
   * @see de.intarsys.monitor.IMonitor#getData()
   */
  public synchronized Map getData() {
    Map attributes = new HashMap();

    // monitor
    attributes.put(ATTR_NAME, getName());
    attributes.put(ATTR_FIRST, new Long(first));
    attributes.put(ATTR_LAST, new Long(last));
    attributes.put(ATTR_MAXACTIVE, new Integer(maxActive));

    // trace statistic
    attributes.put(ATTR_MIN, new Long(statistic.min));
    attributes.put(ATTR_MAX, new Long(statistic.max));
    attributes.put(ATTR_COUNT, new Long(statistic.count));
    attributes.put(ATTR_EFFECTIVE, new Long(statistic.total));
    attributes.put(ATTR_AVG, new Long(statistic.avg));

    return attributes;
  }

  /**
   * The format used to format the samples.
   *
   * @return The format used to format the samples.
   */
  protected Format getFormat() {
    return DEFAULT_FORMAT;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.intarsys.tools.monitor.IMonitor#getFormattedData()
   */
  public synchronized Map getFormattedData() {
    Map attributes = new HashMap();

    // monitor
    attributes.put(ATTR_NAME, getName());
    attributes.put(ATTR_FIRST, getFormat().format(new Long(first)));
    attributes.put(ATTR_LAST, getFormat().format(new Long(last)));
    attributes.put(ATTR_MAXACTIVE, new Integer(maxActive));

    attributes.put(ATTR_MIN, new Long(statistic.min));
    attributes.put(ATTR_MAX, new Long(statistic.max));
    attributes.put(ATTR_COUNT, new Long(statistic.count));
    attributes.put(ATTR_EFFECTIVE, new Long(statistic.total));
    attributes.put(ATTR_AVG, new Long(statistic.avg));
    return attributes;
  }

  /**
   * DOCUMENT ME!
   *
   * @return Returns the relative.
   */
  public boolean isRelative() {
    return relative;
  }

  public void setRelative(boolean relative) {
    this.relative = relative;
  }

  /**
   * Reset the relevant internal state of the monitor to reuse it.
   */
  @Override
  public synchronized void reset() {
    super.reset();
    // monitor
    active = 0;
    first = -1L;
    last = -1L;
    maxActive = 0;
    statistic.reset();
    sampleStatistics = new HashMap();
  }

  /**
   * Get informed that a tracehas been started.
   *
   * @param trace The trace that is started.
   */
  @Override
  protected synchronized void started(ITrace trace) {
    if (first == -1L) {
      first = ((MonitorTrace) trace).getStart();
    }

    active++;

    if (active > maxActive) {
      maxActive = active;
    }
  }

  /**
   * Get informed that a trace has been finished.
   *
   * @param trace The trace that is finished.
   */
  @Override
  protected synchronized void stopped(ITrace trace) {
    last = ((MonitorTrace) trace).getStop();
    active--;

    doCalculation((MonitorTrace) trace);

    super.stopped(trace);
  }

  protected String toFormattedString(String value, int length) {
    if (value == null) {
      value = "";
    }
    if (value.length() > length) {
      return value.substring(0, length);
    }
    StringBuilder sb = new StringBuilder();
    sb.append(value);
    for (int i = value.length(); i < length; i++) {
      sb.append(' ');
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    // StringWriter sw = new StringWriter();
    // Map formattedData = getFormattedData();
    // List keys = new ArrayList(formattedData.keySet());
    // Collections.sort(keys);
    // for (Iterator i = keys.iterator();
    // i.hasNext();
    // ) {
    // String key = (String)i.next();
    // sw.write(key);
    // sw.write(" = ");
    // sw.write(String.valueOf(formattedData.get(key)));
    // sw.write("\n");
    // }
    //
    // return sw.toString();
    return toTableString();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  public String toTableString() {
    StringWriter sw = new StringWriter();
    sw.write(System.getProperty("line.separator"));
    sw.write("   ");
    sw.write(getName());
    sw.write(System.getProperty("line.separator"));
    sw.write(System.getProperty("line.separator"));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString("description", COLWIDTH_LABEL));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString("count", COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString("total", COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString("total %", COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString("min", COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString("max", COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString("avg", COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(System.getProperty("line.separator"));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(TXT_HOR_SEPARATOR, COLWIDTH_LABEL));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(TXT_HOR_SEPARATOR, COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(TXT_HOR_SEPARATOR, COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(TXT_HOR_SEPARATOR, COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(TXT_HOR_SEPARATOR, COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(TXT_HOR_SEPARATOR, COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(TXT_HOR_SEPARATOR, COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(System.getProperty("line.separator"));
    List sampleStatisticList = new ArrayList(sampleStatistics.values());
    Collections.sort(sampleStatisticList);
    for (Iterator it = sampleStatisticList.iterator(); it.hasNext(); ) {
      MonitorStatistic sampleStatistic = (MonitorStatistic) it.next();
      sw.write(TXT_VER_SEPARATOR);
      sw.write(toFormattedString(sampleStatistic.description,
          COLWIDTH_LABEL));
      sw.write(TXT_VER_SEPARATOR);
      sw.write(toFormattedString(String.valueOf(sampleStatistic.count),
          COLWIDTH_NUMBER));
      sw.write(TXT_VER_SEPARATOR);
      sw.write(toFormattedString(String.valueOf(sampleStatistic.total),
          COLWIDTH_NUMBER));
      sw.write(TXT_VER_SEPARATOR);
      float totalPercent = Math.round((float) sampleStatistic.total
          / (float) statistic.total * 10000f) / 100f;
      sw.write(toFormattedString(String.valueOf(totalPercent),
          COLWIDTH_NUMBER));
      sw.write(TXT_VER_SEPARATOR);
      sw.write(toFormattedString(String.valueOf(sampleStatistic.min),
          COLWIDTH_NUMBER));
      sw.write(TXT_VER_SEPARATOR);
      sw.write(toFormattedString(String.valueOf(sampleStatistic.max),
          COLWIDTH_NUMBER));
      sw.write(TXT_VER_SEPARATOR);
      sw.write(toFormattedString(String.valueOf(sampleStatistic.avg),
          COLWIDTH_NUMBER));
      sw.write(TXT_VER_SEPARATOR);
      sw.write(System.getProperty("line.separator"));
    }
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(TXT_HOR_SEPARATOR, COLWIDTH_LABEL));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(TXT_HOR_SEPARATOR, COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(TXT_HOR_SEPARATOR, COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(TXT_HOR_SEPARATOR, COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(TXT_HOR_SEPARATOR, COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(TXT_HOR_SEPARATOR, COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(TXT_HOR_SEPARATOR, COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(System.getProperty("line.separator"));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString("", COLWIDTH_LABEL));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(String.valueOf(statistic.count),
        COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(String.valueOf(statistic.total),
        COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(String.valueOf(100), COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(String.valueOf(statistic.min),
        COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(String.valueOf(statistic.max),
        COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(toFormattedString(String.valueOf(statistic.avg),
        COLWIDTH_NUMBER));
    sw.write(TXT_VER_SEPARATOR);
    sw.write(System.getProperty("line.separator"));
    return sw.toString();
  }
}
