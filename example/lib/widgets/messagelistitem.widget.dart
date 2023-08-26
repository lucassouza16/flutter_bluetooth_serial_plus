import 'package:flutter/material.dart';

class MessageModel {
  final String message;
  final bool isMe;
  final DateTime time;

  const MessageModel({
    required this.message,
    required this.isMe,
    required this.time,
  });
}

class MessageListItem extends StatelessWidget {
  final MessageModel item;

  const MessageListItem({
    super.key,
    required this.item,
  });

  @override
  Widget build(BuildContext context) {
    bool isMe = item.isMe;
    String message = item.message;
    DateTime time = item.time;

    return Container(
      padding: const EdgeInsets.symmetric(vertical: 5, horizontal: 10),
      child: LayoutBuilder(
        builder: (context, constraints) => Row(
          mainAxisAlignment: isMe ? MainAxisAlignment.end : MainAxisAlignment.start,
          children: [
            IntrinsicWidth(
              child: Container(
                constraints: BoxConstraints(
                  minHeight: 50,
                  minWidth: 70,
                  maxWidth: constraints.maxWidth - 40,
                ),
                padding: const EdgeInsets.all(7),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(10),
                  color: item.isMe ? Colors.blue.shade600 : Colors.grey.shade600,
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      message,
                      style: const TextStyle(
                        color: Colors.white,
                      ),
                    ),
                    Text(
                      "${time.hour}:${time.minute}",
                      textAlign: TextAlign.right,
                      style: const TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                      ),
                    )
                  ],
                ),
              ),
            )
          ],
        ),
      ),
    );
  }
}
