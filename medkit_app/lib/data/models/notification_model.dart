class NotificationModel {
  final int idNotification;
  final String type;
  final String title;
  final String message;
  final bool isRead;
  final String? link;
  final DateTime createdAt;

  NotificationModel({
    required this.idNotification,
    required this.type,
    required this.title,
    required this.message,
    required this.isRead,
    this.link,
    required this.createdAt,
  });

  factory NotificationModel.fromJson(Map<String, dynamic> json) {
    return NotificationModel(
      idNotification: json['idNotification'],
      type: json['type'],
      title: json['title'],
      message: json['message'],
      isRead: json['isRead'] ?? false,
      link: json['link'],
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}
