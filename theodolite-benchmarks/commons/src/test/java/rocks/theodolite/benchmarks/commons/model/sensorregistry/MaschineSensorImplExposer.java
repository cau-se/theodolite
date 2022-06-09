package rocks.theodolite.benchmarks.commons.model.sensorregistry;

/**
 * Helper class to allow tests in other packages access {@link MachineSensorImpl} class objects.
 */
public final class MaschineSensorImplExposer {

  public static final Class<? extends MachineSensor> MACHINE_SENSOR_IMPL_CLASS =
      MachineSensorImpl.class;

  private MaschineSensorImplExposer() {}

}
