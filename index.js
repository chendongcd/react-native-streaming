import {
  Platform
} from 'react-native';

var Streaming = require('./Streaming/Streaming');
var Player = require('./Player/Player')
var HorStreaming = require('./Streaming/HorStreaming')

module.exports = {
  Streaming:Streaming,
  Player:Player,
  HorStreaming:Platform.OS=='ios'?HorStreaming:Streaming
}
