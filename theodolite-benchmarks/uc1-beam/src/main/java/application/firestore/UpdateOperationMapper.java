package application.firestore;

import com.google.firestore.v1.Document;
import com.google.firestore.v1.Write;
import org.apache.beam.sdk.transforms.SimpleFunction;

final class UpdateOperationMapper extends SimpleFunction<Document, Write> {

  private static final long serialVersionUID = -5263671231838353748L; // NOPMD

  @Override
  public Write apply(final Document document) {
    return Write.newBuilder()
        .setUpdate(document)
        .build();
  }

}
