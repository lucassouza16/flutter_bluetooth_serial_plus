import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus.dart';
import 'package:flutter_bluetooth_serial_plus_example/widgets/messagelistitem.widget.dart';

class ChatPage extends StatefulWidget {
  final ValueNotifier<List<MessageModel>> messages;

  const ChatPage({
    super.key,
    required this.messages,
  });

  @override
  State<ChatPage> createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {
  final _bluetoothPlugin = FlutterBluetoothSerialPlus.instance;

  final TextEditingController _editingController = TextEditingController();

  late List<MessageModel> _messages;

  final List<String> _commands = [
    "led on",
    "led off",
    "led is on?",
  ];

  @override
  void initState() {
    _messages = widget.messages.value;

    widget.messages.addListener(() {
      if (!mounted) return;
      setState(() => _messages = widget.messages.value);
    });

    super.initState();
  }

  void _handleNewMessage([String? text]) {
    String textMessage = text ?? _editingController.text;

    if (textMessage.isEmpty) return;

    var message = MessageModel(
      message: textMessage,
      time: DateTime.now(),
      type: MessageModel.isMe,
    );

    _editingController.clear();

    widget.messages.value = [
      message,
      ..._messages,
    ];

    _bluetoothPlugin.write(Uint8List.fromList(utf8.encode(textMessage)));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Chat")),
      body: Column(children: [
        Expanded(
          child: ListView.builder(
            itemCount: _messages.length,
            reverse: true,
            itemBuilder: (context, index) {
              var item = _messages[index];

              return MessageListItem(
                item: item,
              );
            },
          ),
        ),
        Container(
          decoration: const BoxDecoration(color: Colors.blue),
          child: Column(
            children: [
              SizedBox(
                height: 35,
                width: double.infinity,
                child: ListView.builder(
                  scrollDirection: Axis.horizontal,
                  itemCount: _commands.length,
                  itemBuilder: (context, index) {
                    String command = _commands[index];

                    return InkWell(
                      child: Container(
                        padding: const EdgeInsets.symmetric(vertical: 5, horizontal: 10),
                        margin: const EdgeInsets.only(left: 7, top: 6),
                        decoration: BoxDecoration(
                          color: Colors.blue.shade700,
                          borderRadius: BorderRadius.circular(7),
                        ),
                        child: Text(
                          command,
                          style: const TextStyle(
                            color: Colors.white,
                          ),
                        ),
                      ),
                      onTap: () => _handleNewMessage(command),
                    );
                  },
                ),
              ),
              Container(
                height: 55,
                margin: const EdgeInsets.all(10),
                padding: const EdgeInsets.all(4),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(10),
                  color: Colors.white,
                ),
                child: Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _editingController,
                        decoration: const InputDecoration(
                          border: InputBorder.none,
                          hintText: "Message here",
                        ),
                      ),
                    ),
                    IconButton(
                      onPressed: _handleNewMessage,
                      icon: const Icon(Icons.send),
                    )
                  ],
                ),
              )
            ],
          ),
        )
      ]),
    );
  }
}
