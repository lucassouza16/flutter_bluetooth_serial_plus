// ignore_for_file: constant_identifier_names

import 'package:flutter/material.dart';

class MessageModel {
  static const isMe = 0;
  static const isOther = 1;
  static const isInfo = 2;

  final String message;
  final int type;
  final DateTime time;

  const MessageModel({
    required this.message,
    required this.type,
    required this.time,
  });
}

class MessageListItem extends StatelessWidget {
  final MessageModel item;

  const MessageListItem({
    super.key,
    required this.item,
  });

  String _addZero(int value) => "${value > 9 ? "" : 0}$value";

  String _formatTime(DateTime time) => "${time.hour}:${_addZero(time.minute)}";

  @override
  Widget build(BuildContext context) {
    int type = item.type;
    String message = item.message;
    DateTime time = item.time;

    return Container(
      padding: const EdgeInsets.symmetric(vertical: 5, horizontal: 10),
      child: LayoutBuilder(
        builder: (context, constraints) => Row(
          mainAxisAlignment: type == MessageModel.isMe
              ? MainAxisAlignment.end
              : type == MessageModel.isOther
                  ? MainAxisAlignment.start
                  : MainAxisAlignment.center,
          children: [
            IntrinsicWidth(
              child: Container(
                constraints: BoxConstraints(
                  minWidth: 80,
                  maxWidth: constraints.maxWidth - 40,
                ),
                padding: const EdgeInsets.all(7),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(10),
                  color: type == MessageModel.isMe
                      ? Colors.blue.shade600
                      : type == MessageModel.isOther
                          ? Colors.grey.shade600
                          : Colors.green,
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
                    if (type != MessageModel.isInfo)
                      Container(
                        margin: const EdgeInsets.only(top: 5),
                        child: Text(
                          _formatTime(time),
                          textAlign: TextAlign.right,
                          style: const TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.bold,
                          ),
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
