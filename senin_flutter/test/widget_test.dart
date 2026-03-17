import 'package:flutter_test/flutter_test.dart';
import 'package:senin_flutter/src/app.dart';

void main() {
  testWidgets('renders Senin app shell', (tester) async {
    await tester.pumpWidget(const SeninApp());

    expect(find.text('Senin'), findsOneWidget);
    expect(find.text('Schimbă orașul'), findsOneWidget);
  });
}
