import 'package:flutter/material.dart';
import 'dart:math';

class CircularWavePainter extends CustomPainter {
  final double waveValue;
  final Color progressColor;
  final Color backgroundColor;
  final double progress;
  final double strokeWidth;
  final double amplitudeValue;
  final double gapAngleValue;
  final double gapAngleStartValue;

  const CircularWavePainter(
    this.waveValue,
    this.progressColor,
    this.backgroundColor,
    this.progress,
    this.strokeWidth,
    this.amplitudeValue,
    this.gapAngleValue,
    this.gapAngleStartValue,
  );

  @override
  void paint(Canvas canvas, Size size) {
    final radius = size.width / 2;
    final center = Offset(radius, radius);

    final progressPaint = Paint()
      ..color = progressColor
      ..strokeWidth = strokeWidth
      ..strokeCap = StrokeCap.round
      ..style = PaintingStyle.stroke;

    final bgPaint = Paint()
      ..color = backgroundColor
      ..strokeWidth = strokeWidth
      ..strokeCap = StrokeCap.round
      ..style = PaintingStyle.stroke;

    final amplitude = progress < 0.02 ? 0 : amplitudeValue;
    final frequency = progress < 0.02 ? 0 : 12.0;

    final sweepAngle = 2 * pi;
    final gapAngle = progress < gapAngleStartValue ? 0 : gapAngleValue;

    Path progressPath = Path();
    Path backgroundPath = Path();

    bool startedProgress = false;
    bool startedBackground = false;

    for (double angle = 0; angle <= sweepAngle; angle += 0.02) {
      final shiftedAngle = angle - pi / 2;

      final wave = amplitude * sin((angle * frequency) + (waveValue * 2 * pi));

      final baseX = center.dx + (radius - 10) * cos(shiftedAngle);
      final baseY = center.dy + (radius - 10) * sin(shiftedAngle);

      final waveX = center.dx + (radius - 10 + wave) * cos(shiftedAngle);
      final waveY = center.dy + (radius - 10 + wave) * sin(shiftedAngle);

      final normalized = angle / sweepAngle;

      if (normalized >= (gapAngle / sweepAngle) &&
          normalized <= progress - (gapAngle / sweepAngle)) {
        if (!startedProgress) {
          progressPath.moveTo(waveX, waveY);
          startedProgress = true;
        } else {
          progressPath.lineTo(waveX, waveY);
        }
      } else if (normalized >= progress + (gapAngle / sweepAngle) &&
          normalized <= 1 - (gapAngle / sweepAngle)) {
        if (!startedBackground) {
          backgroundPath.moveTo(baseX, baseY);
          startedBackground = true;
        } else {
          backgroundPath.lineTo(baseX, baseY);
        }
      }
    }

    canvas.drawPath(backgroundPath, bgPaint);
    canvas.drawPath(progressPath, progressPaint);
  }

  @override
  bool shouldRepaint(CircularWavePainter oldDelegate) =>
      oldDelegate.waveValue != waveValue ||
      oldDelegate.progress != progress ||
      oldDelegate.progressColor != progressColor ||
      oldDelegate.backgroundColor != backgroundColor;
}

class WavyCircularProgress extends StatefulWidget {
  final double progress;
  final double strokeWidth;
  final double amplitude;
  final double gapAngle;
  final double gapAngleStartValue;

  const WavyCircularProgress({
    super.key,
    required this.progress,
    this.strokeWidth = 14,
    this.amplitude = 5.0,
    this.gapAngle = 0.045,
    this.gapAngleStartValue = 0.02,
  });

  @override
  State<WavyCircularProgress> createState() => _WavyCircularProgressState();
}

class _WavyCircularProgressState extends State<WavyCircularProgress>
    with SingleTickerProviderStateMixin {
  late final AnimationController _waveController;

  @override
  void initState() {
    super.initState();
    _waveController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 2),
    )..repeat();
  }

  @override
  void dispose() {
    _waveController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final colorTheme = Theme.of(context).colorScheme;

    return AnimatedBuilder(
      animation: _waveController,
      builder: (_, __) {
        return TweenAnimationBuilder<double>(
          tween: Tween<double>(begin: 0, end: widget.progress),
          duration: const Duration(milliseconds: 600),
          curve: Curves.easeInOut,
          builder: (context, animatedProgress, child) {
            return CustomPaint(
              size: const Size(150, 150),
              painter: CircularWavePainter(
                _waveController.value,
                colorTheme.primary,
                colorTheme.secondaryContainer,
                animatedProgress,
                widget.strokeWidth,
                widget.amplitude,
                widget.gapAngle,
                widget.gapAngleStartValue,
              ),
            );
          },
        );
      },
    );
  }
}
