import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_bluetooth_serial_plus/flutter_bluetooth_serial_plus.dart';
import 'package:flutter_bluetooth_serial_plus_example/widgets/messagelistitem.widget.dart';

class ChatPage extends StatefulWidget {
  const ChatPage({super.key});

  @override
  State<ChatPage> createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {
  final _bluetoothPlugin = FlutterBluetoothSerialPlus.instance;

  final TextEditingController _editingController = TextEditingController();

  final List<MessageModel> _messages = [];

  @override
  void initState() {
    _bluetoothPlugin.read.listen((event) {
      setState(() {
        _messages.insert(
            0,
            MessageModel(
              message: utf8.decode(event),
              time: DateTime.now(),
              isMe: false,
            ));
      });
    });

    super.initState();
  }

  void _handleNewMessage() {
    var message = MessageModel(
      message: _editingController.text,
      time: DateTime.now(),
      isMe: true,
    );

    _editingController.clear();

    setState(() {
      _messages.insert(0, message);
    });

    _bluetoothPlugin.write(Uint8List.fromList(utf8.encode(message.message)));
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
          height: 70,
          width: double.infinity,
          padding: const EdgeInsets.all(10),
          decoration: const BoxDecoration(color: Colors.blue),
          child: Container(
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
                )),
                IconButton(
                  onPressed: _handleNewMessage,
                  icon: const Icon(Icons.send),
                )
              ],
            ),
          ),
        )
      ]),
    );
  }
}
