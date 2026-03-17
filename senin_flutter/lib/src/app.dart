import 'package:flutter/material.dart';

import 'home_page.dart';

class SeninApp extends StatelessWidget {
  const SeninApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Senin',
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF2C93CB),
          brightness: Brightness.light,
        ),
        scaffoldBackgroundColor: const Color(0xFFF3EEE6),
        textTheme: Typography.blackCupertino,
      ),
      home: const HomePage(),
    );
  }
}
