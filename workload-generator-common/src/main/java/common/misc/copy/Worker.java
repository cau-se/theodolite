package common.misc.copy;

/*
 * Wrapper class for a worker.
 */
public class Worker {

  private final int id;

  /**
   * Create a new worker with an {@code id}
   * 
   * @param id the id of the worker.
   */
  public Worker(final int id) {
    super();
    this.id = id;
  }

  public int getId() {
    return this.id;
  }

}
