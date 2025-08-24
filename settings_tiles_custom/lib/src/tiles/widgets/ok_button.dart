// ignore_for_file: public_member_api_docs private class

import 'package:flutter/material.dart';

class OkButton extends StatelessWidget {
  const OkButton({
    required this.onPressed,
    super.key,
  });

  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) {
    return TextButton(
      onPressed: onPressed,
      child: const Text('OK', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700)),
    );
  }
}
