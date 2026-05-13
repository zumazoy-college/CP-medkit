import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import 'dart:io';
import 'package:path_provider/path_provider.dart';
import '../../../data/models/certificate_model.dart';
import '../../providers/certificate_provider.dart';
import '../../../core/utils/date_formatter.dart';

class CertificatesScreen extends StatefulWidget {
  const CertificatesScreen({Key? key}) : super(key: key);

  @override
  State<CertificatesScreen> createState() => _CertificatesScreenState();
}

class _CertificatesScreenState extends State<CertificatesScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadCertificates();
    });
  }

  Future<void> _loadCertificates() async {
    final provider = Provider.of<CertificateProvider>(context, listen: false);
    await provider.loadMyCertificates();
  }

  Future<void> _downloadCertificate(CertificateModel certificate) async {
    try {
      final provider = Provider.of<CertificateProvider>(context, listen: false);

      // Download certificate
      final bytes = await provider.downloadCertificate(certificate.idCertificate);

      // Get downloads directory
      Directory? directory;
      if (Platform.isAndroid) {
        directory = Directory('/storage/emulated/0/Download');
        if (!await directory.exists()) {
          directory = await getExternalStorageDirectory();
        }
      } else {
        directory = await getApplicationDocumentsDirectory();
      }

      if (directory == null) {
        throw Exception('Не удалось получить директорию для сохранения');
      }

      // Create file name
      final fileName = '${certificate.certificateTypeName}_${DateFormat('yyyy-MM-dd').format(certificate.createdAt)}.pdf';
      final file = File('${directory.path}/$fileName');

      // Write file
      await file.writeAsBytes(bytes);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Справка сохранена: ${file.path}')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Ошибка скачивания: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Мои справки'),
        backgroundColor: Colors.blue,
      ),
      body: Consumer<CertificateProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.error != null) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    'Ошибка загрузки справок',
                    style: TextStyle(color: Colors.red, fontSize: 16),
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: _loadCertificates,
                    child: const Text('Повторить'),
                  ),
                ],
              ),
            );
          }

          if (provider.certificates.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.description_outlined, size: 64, color: Colors.grey),
                  const SizedBox(height: 16),
                  Text(
                    'У вас пока нет справок',
                    style: TextStyle(fontSize: 16, color: Colors.grey[600]),
                  ),
                ],
              ),
            );
          }

          return RefreshIndicator(
            onRefresh: _loadCertificates,
            child: ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: provider.certificates.length,
              itemBuilder: (context, index) {
                final certificate = provider.certificates[index];
                return _buildCertificateCard(certificate);
              },
            ),
          );
        },
      ),
    );
  }

  Widget _buildCertificateCard(CertificateModel certificate) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.description, color: Colors.blue, size: 24),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    certificate.certificateTypeName,
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            _buildInfoRow(
              Icons.person,
              'Врач',
              '${certificate.doctorFullName}, ${certificate.doctorSpecialization}',
            ),
            const SizedBox(height: 8),
            _buildInfoRow(
              Icons.calendar_today,
              'Создана',
              DateFormatter.formatDate(certificate.createdAt),
            ),
            if (certificate.validFrom != null && certificate.validTo != null) ...[
              const SizedBox(height: 8),
              _buildInfoRow(
                Icons.event_available,
                'Действительна',
                '${DateFormatter.formatDate(certificate.validFrom!)} - ${DateFormatter.formatDate(certificate.validTo!)}',
              ),
            ],
            if (certificate.disabilityPeriodFrom != null &&
                certificate.disabilityPeriodTo != null) ...[
              const SizedBox(height: 8),
              _buildInfoRow(
                Icons.medical_services,
                'Период нетрудоспособности',
                '${DateFormatter.formatDate(certificate.disabilityPeriodFrom!)} - ${DateFormatter.formatDate(certificate.disabilityPeriodTo!)}',
              ),
            ],
            if (certificate.workRestrictions != null &&
                certificate.workRestrictions!.isNotEmpty) ...[
              const SizedBox(height: 8),
              _buildInfoRow(
                Icons.info_outline,
                'Рекомендации',
                certificate.workRestrictions!,
              ),
            ],
            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: () => _downloadCertificate(certificate),
                icon: const Icon(Icons.download),
                label: const Text('Скачать PDF'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.blue,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 12),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(IconData icon, String label, String value) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, size: 16, color: Colors.grey[600]),
        const SizedBox(width: 8),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: TextStyle(
                  fontSize: 12,
                  color: Colors.grey[600],
                ),
              ),
              const SizedBox(height: 2),
              Text(
                value,
                style: const TextStyle(
                  fontSize: 14,
                  color: Colors.black87,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
