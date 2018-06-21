import React, {
    Component
} from 'react';
import PropTypes from 'prop-types'
import {
    requireNativeComponent,
    View,
    NativeModules,
    Platform
} from 'react-native';

const _PlayerManager = NativeModules.PlayerManager

class Player extends Component {

    constructor(props, context) {
        super(props, context);
        this._onFullScreen = this._onFullScreen.bind(this);
        this._onClick = this._onClick.bind(this);
        this._onPlayState = this._onPlayState.bind(this);
        this._onErrorConnect = this._onErrorConnect.bind(this)
		this._onErrorPlayerOpen = this._onErrorPlayerOpen.bind(this)
		this._onErrorDrag = this._onErrorDrag.bind(this)
		this._onErrorDecoded = this._onErrorDecoded.bind(this)
    }

    _onFullScreen(event) {
        this.props.onFullScreen && this.props.onFullScreen(event.nativeEvent);
    }
    _onClick(event) {
        this.props.onClick && this.props.onClick(event.nativeEvent);
    }
    _onPlayState(event) {
        this.props.onPlayState && this.props.onPlayState(event.nativeEvent);
    }

    static setRemove() {
        Platform=='ios'?false: _PlayerManager.removePlayer()
    }

    static stopPlayer () {
        _PlayerManager.stopPlayer()
    }
    static reload () {
        _PlayerManager.reload()
    }
    static actionFull () {
        _PlayerManager.actionFull()
    }
    _onErrorConnect(event) {
		this.props.onErrorConnect && this.props.onErrorConnect(event.nativeEvent);
	}

	_onErrorPlayerOpen(event) {
		this.props.onErrorPlayerOpen && this.props.onErrorPlayerOpen(event.nativeEvent);
	}

	_onErrorDrag(event) {
		this.props.onErrorDrag && this.props.onErrorDrag(event.nativeEvent);
	}

	_onErrorDecoded(event) {
		this.props.onErrorDecoded && this.props.onErrorDecoded(event.nativeEvent);
	}


    render() {
        const nativeProps = Object.assign({}, this.props);
        Object.assign(nativeProps, {
            onFullScreen:this._onFullScreen,
            onClick:this._onClick,
            onPlayState:this._onPlayState,
			onErrorConnect: this._onErrorConnect,
			onErrorPlayerOpen: this._onErrorPlayerOpen,
			onErrorDrag: this._onErrorDrag,
			onErrorDecoded: this._onErrorDecoded
        });
        return (
            <RCTPlayer
                {...nativeProps}
            />
        )
    }
}

Player.propTypes = {
    source: PropTypes.shape({                          // 是否符合指定格式的物件
        uri: PropTypes.string.isRequired,
        holder: PropTypes.string,
        timeout: PropTypes.number, //Android only
        hardCodec: PropTypes.bool, //Android only
        live: PropTypes.bool, //Android only
    }).isRequired,
    started:PropTypes.bool,
    muted:PropTypes.bool, //iOS only
    lock:PropTypes.bool,
    aspectRatio: PropTypes.oneOf([0, 1, 2, 3, 4]),
    onFullScreen: PropTypes.func,
    onClick: PropTypes.func,
    onPlayState: PropTypes.func,//0:stop 1:playing 2:pasued 3:interrupted 4:forward(快进) 5:backward(后退),
    stopPlayback:PropTypes.bool,
    pause:PropTypes.bool,
    ...View.propTypes,
}

const RCTPlayer = requireNativeComponent('RCTPlayer', Player);

module.exports = Player;
