/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package theodolite.commons.model.records;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@org.apache.avro.specific.AvroGenerated
public class YearActivePowerRecord extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -4368523068038395628L;


  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"YearActivePowerRecord\",\"namespace\":\"theodolite.commons.model.records\",\"fields\":[{\"name\":\"identifier\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"year\",\"type\":\"int\"},{\"name\":\"periodStart\",\"type\":\"long\"},{\"name\":\"periodEnd\",\"type\":\"long\"},{\"name\":\"count\",\"type\":\"long\"},{\"name\":\"mean\",\"type\":\"double\"},{\"name\":\"populationVariance\",\"type\":\"double\"},{\"name\":\"min\",\"type\":\"double\"},{\"name\":\"max\",\"type\":\"double\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static final SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<YearActivePowerRecord> ENCODER =
      new BinaryMessageEncoder<YearActivePowerRecord>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<YearActivePowerRecord> DECODER =
      new BinaryMessageDecoder<YearActivePowerRecord>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<YearActivePowerRecord> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<YearActivePowerRecord> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<YearActivePowerRecord> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<YearActivePowerRecord>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this YearActivePowerRecord to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a YearActivePowerRecord from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a YearActivePowerRecord instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static YearActivePowerRecord fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  private java.lang.String identifier;
  private int year;
  private long periodStart;
  private long periodEnd;
  private long count;
  private double mean;
  private double populationVariance;
  private double min;
  private double max;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public YearActivePowerRecord() {}

  /**
   * All-args constructor.
   * @param identifier The new value for identifier
   * @param year The new value for year
   * @param periodStart The new value for periodStart
   * @param periodEnd The new value for periodEnd
   * @param count The new value for count
   * @param mean The new value for mean
   * @param populationVariance The new value for populationVariance
   * @param min The new value for min
   * @param max The new value for max
   */
  public YearActivePowerRecord(java.lang.String identifier, java.lang.Integer year, java.lang.Long periodStart, java.lang.Long periodEnd, java.lang.Long count, java.lang.Double mean, java.lang.Double populationVariance, java.lang.Double min, java.lang.Double max) {
    this.identifier = identifier;
    this.year = year;
    this.periodStart = periodStart;
    this.periodEnd = periodEnd;
    this.count = count;
    this.mean = mean;
    this.populationVariance = populationVariance;
    this.min = min;
    this.max = max;
  }

  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return identifier;
    case 1: return year;
    case 2: return periodStart;
    case 3: return periodEnd;
    case 4: return count;
    case 5: return mean;
    case 6: return populationVariance;
    case 7: return min;
    case 8: return max;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: identifier = value$ != null ? value$.toString() : null; break;
    case 1: year = (java.lang.Integer)value$; break;
    case 2: periodStart = (java.lang.Long)value$; break;
    case 3: periodEnd = (java.lang.Long)value$; break;
    case 4: count = (java.lang.Long)value$; break;
    case 5: mean = (java.lang.Double)value$; break;
    case 6: populationVariance = (java.lang.Double)value$; break;
    case 7: min = (java.lang.Double)value$; break;
    case 8: max = (java.lang.Double)value$; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'identifier' field.
   * @return The value of the 'identifier' field.
   */
  public java.lang.String getIdentifier() {
    return identifier;
  }



  /**
   * Gets the value of the 'year' field.
   * @return The value of the 'year' field.
   */
  public int getYear() {
    return year;
  }



  /**
   * Gets the value of the 'periodStart' field.
   * @return The value of the 'periodStart' field.
   */
  public long getPeriodStart() {
    return periodStart;
  }



  /**
   * Gets the value of the 'periodEnd' field.
   * @return The value of the 'periodEnd' field.
   */
  public long getPeriodEnd() {
    return periodEnd;
  }



  /**
   * Gets the value of the 'count' field.
   * @return The value of the 'count' field.
   */
  public long getCount() {
    return count;
  }



  /**
   * Gets the value of the 'mean' field.
   * @return The value of the 'mean' field.
   */
  public double getMean() {
    return mean;
  }



  /**
   * Gets the value of the 'populationVariance' field.
   * @return The value of the 'populationVariance' field.
   */
  public double getPopulationVariance() {
    return populationVariance;
  }



  /**
   * Gets the value of the 'min' field.
   * @return The value of the 'min' field.
   */
  public double getMin() {
    return min;
  }



  /**
   * Gets the value of the 'max' field.
   * @return The value of the 'max' field.
   */
  public double getMax() {
    return max;
  }



  /**
   * Creates a new YearActivePowerRecord RecordBuilder.
   * @return A new YearActivePowerRecord RecordBuilder
   */
  public static theodolite.commons.model.records.YearActivePowerRecord.Builder newBuilder() {
    return new theodolite.commons.model.records.YearActivePowerRecord.Builder();
  }

  /**
   * Creates a new YearActivePowerRecord RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new YearActivePowerRecord RecordBuilder
   */
  public static theodolite.commons.model.records.YearActivePowerRecord.Builder newBuilder(theodolite.commons.model.records.YearActivePowerRecord.Builder other) {
    if (other == null) {
      return new theodolite.commons.model.records.YearActivePowerRecord.Builder();
    } else {
      return new theodolite.commons.model.records.YearActivePowerRecord.Builder(other);
    }
  }

  /**
   * Creates a new YearActivePowerRecord RecordBuilder by copying an existing YearActivePowerRecord instance.
   * @param other The existing instance to copy.
   * @return A new YearActivePowerRecord RecordBuilder
   */
  public static theodolite.commons.model.records.YearActivePowerRecord.Builder newBuilder(theodolite.commons.model.records.YearActivePowerRecord other) {
    if (other == null) {
      return new theodolite.commons.model.records.YearActivePowerRecord.Builder();
    } else {
      return new theodolite.commons.model.records.YearActivePowerRecord.Builder(other);
    }
  }

  /**
   * RecordBuilder for YearActivePowerRecord instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<YearActivePowerRecord>
    implements org.apache.avro.data.RecordBuilder<YearActivePowerRecord> {

    private java.lang.String identifier;
    private int year;
    private long periodStart;
    private long periodEnd;
    private long count;
    private double mean;
    private double populationVariance;
    private double min;
    private double max;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$, MODEL$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(theodolite.commons.model.records.YearActivePowerRecord.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.identifier)) {
        this.identifier = data().deepCopy(fields()[0].schema(), other.identifier);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.year)) {
        this.year = data().deepCopy(fields()[1].schema(), other.year);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (isValidValue(fields()[2], other.periodStart)) {
        this.periodStart = data().deepCopy(fields()[2].schema(), other.periodStart);
        fieldSetFlags()[2] = other.fieldSetFlags()[2];
      }
      if (isValidValue(fields()[3], other.periodEnd)) {
        this.periodEnd = data().deepCopy(fields()[3].schema(), other.periodEnd);
        fieldSetFlags()[3] = other.fieldSetFlags()[3];
      }
      if (isValidValue(fields()[4], other.count)) {
        this.count = data().deepCopy(fields()[4].schema(), other.count);
        fieldSetFlags()[4] = other.fieldSetFlags()[4];
      }
      if (isValidValue(fields()[5], other.mean)) {
        this.mean = data().deepCopy(fields()[5].schema(), other.mean);
        fieldSetFlags()[5] = other.fieldSetFlags()[5];
      }
      if (isValidValue(fields()[6], other.populationVariance)) {
        this.populationVariance = data().deepCopy(fields()[6].schema(), other.populationVariance);
        fieldSetFlags()[6] = other.fieldSetFlags()[6];
      }
      if (isValidValue(fields()[7], other.min)) {
        this.min = data().deepCopy(fields()[7].schema(), other.min);
        fieldSetFlags()[7] = other.fieldSetFlags()[7];
      }
      if (isValidValue(fields()[8], other.max)) {
        this.max = data().deepCopy(fields()[8].schema(), other.max);
        fieldSetFlags()[8] = other.fieldSetFlags()[8];
      }
    }

    /**
     * Creates a Builder by copying an existing YearActivePowerRecord instance
     * @param other The existing instance to copy.
     */
    private Builder(theodolite.commons.model.records.YearActivePowerRecord other) {
      super(SCHEMA$, MODEL$);
      if (isValidValue(fields()[0], other.identifier)) {
        this.identifier = data().deepCopy(fields()[0].schema(), other.identifier);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.year)) {
        this.year = data().deepCopy(fields()[1].schema(), other.year);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.periodStart)) {
        this.periodStart = data().deepCopy(fields()[2].schema(), other.periodStart);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.periodEnd)) {
        this.periodEnd = data().deepCopy(fields()[3].schema(), other.periodEnd);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.count)) {
        this.count = data().deepCopy(fields()[4].schema(), other.count);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.mean)) {
        this.mean = data().deepCopy(fields()[5].schema(), other.mean);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.populationVariance)) {
        this.populationVariance = data().deepCopy(fields()[6].schema(), other.populationVariance);
        fieldSetFlags()[6] = true;
      }
      if (isValidValue(fields()[7], other.min)) {
        this.min = data().deepCopy(fields()[7].schema(), other.min);
        fieldSetFlags()[7] = true;
      }
      if (isValidValue(fields()[8], other.max)) {
        this.max = data().deepCopy(fields()[8].schema(), other.max);
        fieldSetFlags()[8] = true;
      }
    }

    /**
      * Gets the value of the 'identifier' field.
      * @return The value.
      */
    public java.lang.String getIdentifier() {
      return identifier;
    }


    /**
      * Sets the value of the 'identifier' field.
      * @param value The value of 'identifier'.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder setIdentifier(java.lang.String value) {
      validate(fields()[0], value);
      this.identifier = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'identifier' field has been set.
      * @return True if the 'identifier' field has been set, false otherwise.
      */
    public boolean hasIdentifier() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'identifier' field.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder clearIdentifier() {
      identifier = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'year' field.
      * @return The value.
      */
    public int getYear() {
      return year;
    }


    /**
      * Sets the value of the 'year' field.
      * @param value The value of 'year'.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder setYear(int value) {
      validate(fields()[1], value);
      this.year = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'year' field has been set.
      * @return True if the 'year' field has been set, false otherwise.
      */
    public boolean hasYear() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'year' field.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder clearYear() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'periodStart' field.
      * @return The value.
      */
    public long getPeriodStart() {
      return periodStart;
    }


    /**
      * Sets the value of the 'periodStart' field.
      * @param value The value of 'periodStart'.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder setPeriodStart(long value) {
      validate(fields()[2], value);
      this.periodStart = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'periodStart' field has been set.
      * @return True if the 'periodStart' field has been set, false otherwise.
      */
    public boolean hasPeriodStart() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'periodStart' field.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder clearPeriodStart() {
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'periodEnd' field.
      * @return The value.
      */
    public long getPeriodEnd() {
      return periodEnd;
    }


    /**
      * Sets the value of the 'periodEnd' field.
      * @param value The value of 'periodEnd'.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder setPeriodEnd(long value) {
      validate(fields()[3], value);
      this.periodEnd = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'periodEnd' field has been set.
      * @return True if the 'periodEnd' field has been set, false otherwise.
      */
    public boolean hasPeriodEnd() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'periodEnd' field.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder clearPeriodEnd() {
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'count' field.
      * @return The value.
      */
    public long getCount() {
      return count;
    }


    /**
      * Sets the value of the 'count' field.
      * @param value The value of 'count'.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder setCount(long value) {
      validate(fields()[4], value);
      this.count = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'count' field has been set.
      * @return True if the 'count' field has been set, false otherwise.
      */
    public boolean hasCount() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'count' field.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder clearCount() {
      fieldSetFlags()[4] = false;
      return this;
    }

    /**
      * Gets the value of the 'mean' field.
      * @return The value.
      */
    public double getMean() {
      return mean;
    }


    /**
      * Sets the value of the 'mean' field.
      * @param value The value of 'mean'.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder setMean(double value) {
      validate(fields()[5], value);
      this.mean = value;
      fieldSetFlags()[5] = true;
      return this;
    }

    /**
      * Checks whether the 'mean' field has been set.
      * @return True if the 'mean' field has been set, false otherwise.
      */
    public boolean hasMean() {
      return fieldSetFlags()[5];
    }


    /**
      * Clears the value of the 'mean' field.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder clearMean() {
      fieldSetFlags()[5] = false;
      return this;
    }

    /**
      * Gets the value of the 'populationVariance' field.
      * @return The value.
      */
    public double getPopulationVariance() {
      return populationVariance;
    }


    /**
      * Sets the value of the 'populationVariance' field.
      * @param value The value of 'populationVariance'.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder setPopulationVariance(double value) {
      validate(fields()[6], value);
      this.populationVariance = value;
      fieldSetFlags()[6] = true;
      return this;
    }

    /**
      * Checks whether the 'populationVariance' field has been set.
      * @return True if the 'populationVariance' field has been set, false otherwise.
      */
    public boolean hasPopulationVariance() {
      return fieldSetFlags()[6];
    }


    /**
      * Clears the value of the 'populationVariance' field.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder clearPopulationVariance() {
      fieldSetFlags()[6] = false;
      return this;
    }

    /**
      * Gets the value of the 'min' field.
      * @return The value.
      */
    public double getMin() {
      return min;
    }


    /**
      * Sets the value of the 'min' field.
      * @param value The value of 'min'.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder setMin(double value) {
      validate(fields()[7], value);
      this.min = value;
      fieldSetFlags()[7] = true;
      return this;
    }

    /**
      * Checks whether the 'min' field has been set.
      * @return True if the 'min' field has been set, false otherwise.
      */
    public boolean hasMin() {
      return fieldSetFlags()[7];
    }


    /**
      * Clears the value of the 'min' field.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder clearMin() {
      fieldSetFlags()[7] = false;
      return this;
    }

    /**
      * Gets the value of the 'max' field.
      * @return The value.
      */
    public double getMax() {
      return max;
    }


    /**
      * Sets the value of the 'max' field.
      * @param value The value of 'max'.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder setMax(double value) {
      validate(fields()[8], value);
      this.max = value;
      fieldSetFlags()[8] = true;
      return this;
    }

    /**
      * Checks whether the 'max' field has been set.
      * @return True if the 'max' field has been set, false otherwise.
      */
    public boolean hasMax() {
      return fieldSetFlags()[8];
    }


    /**
      * Clears the value of the 'max' field.
      * @return This builder.
      */
    public theodolite.commons.model.records.YearActivePowerRecord.Builder clearMax() {
      fieldSetFlags()[8] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public YearActivePowerRecord build() {
      try {
        YearActivePowerRecord record = new YearActivePowerRecord();
        record.identifier = fieldSetFlags()[0] ? this.identifier : (java.lang.String) defaultValue(fields()[0]);
        record.year = fieldSetFlags()[1] ? this.year : (java.lang.Integer) defaultValue(fields()[1]);
        record.periodStart = fieldSetFlags()[2] ? this.periodStart : (java.lang.Long) defaultValue(fields()[2]);
        record.periodEnd = fieldSetFlags()[3] ? this.periodEnd : (java.lang.Long) defaultValue(fields()[3]);
        record.count = fieldSetFlags()[4] ? this.count : (java.lang.Long) defaultValue(fields()[4]);
        record.mean = fieldSetFlags()[5] ? this.mean : (java.lang.Double) defaultValue(fields()[5]);
        record.populationVariance = fieldSetFlags()[6] ? this.populationVariance : (java.lang.Double) defaultValue(fields()[6]);
        record.min = fieldSetFlags()[7] ? this.min : (java.lang.Double) defaultValue(fields()[7]);
        record.max = fieldSetFlags()[8] ? this.max : (java.lang.Double) defaultValue(fields()[8]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<YearActivePowerRecord>
    WRITER$ = (org.apache.avro.io.DatumWriter<YearActivePowerRecord>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<YearActivePowerRecord>
    READER$ = (org.apache.avro.io.DatumReader<YearActivePowerRecord>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

  @Override protected boolean hasCustomCoders() { return true; }

  @Override public void customEncode(org.apache.avro.io.Encoder out)
    throws java.io.IOException
  {
    out.writeString(this.identifier);

    out.writeInt(this.year);

    out.writeLong(this.periodStart);

    out.writeLong(this.periodEnd);

    out.writeLong(this.count);

    out.writeDouble(this.mean);

    out.writeDouble(this.populationVariance);

    out.writeDouble(this.min);

    out.writeDouble(this.max);

  }

  @Override public void customDecode(org.apache.avro.io.ResolvingDecoder in)
    throws java.io.IOException
  {
    org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
    if (fieldOrder == null) {
      this.identifier = in.readString();

      this.year = in.readInt();

      this.periodStart = in.readLong();

      this.periodEnd = in.readLong();

      this.count = in.readLong();

      this.mean = in.readDouble();

      this.populationVariance = in.readDouble();

      this.min = in.readDouble();

      this.max = in.readDouble();

    } else {
      for (int i = 0; i < 9; i++) {
        switch (fieldOrder[i].pos()) {
        case 0:
          this.identifier = in.readString();
          break;

        case 1:
          this.year = in.readInt();
          break;

        case 2:
          this.periodStart = in.readLong();
          break;

        case 3:
          this.periodEnd = in.readLong();
          break;

        case 4:
          this.count = in.readLong();
          break;

        case 5:
          this.mean = in.readDouble();
          break;

        case 6:
          this.populationVariance = in.readDouble();
          break;

        case 7:
          this.min = in.readDouble();
          break;

        case 8:
          this.max = in.readDouble();
          break;

        default:
          throw new java.io.IOException("Corrupt ResolvingDecoder.");
        }
      }
    }
  }
}










