enum PrintType { barcode, qr, text, bitmap }

class PrintRowData {
  final String content;
  final double paddingLeft;
  final double paddingToTopOfInvoice;
  final PrintType contentType;

  /// font is 0 -> 8
  final String font = "2";

  /// width will apply for barcode and qr, not for text
  final double width;

  /// height will apply for barcode and qr, not for text
  final double height;

  PrintRowData(
      {required this.content,
      this.paddingLeft = 10,
      this.width = 100,
      this.height = 100,
      this.contentType = PrintType.text,
      required this.paddingToTopOfInvoice});
  toJson() {
    return {
      'width': width,
      'height': height,
      'contentType': contentType.toString(),
      'text': content,
      'paddingLeft': paddingLeft,
      'paddingToTopOfInvoice': paddingToTopOfInvoice,
      'font': font
    };
  }
}
