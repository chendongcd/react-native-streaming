/**
 * Created by luojian on 2018/3/22.
 */

import React, {
    Component,
} from 'react';
const PropTypes = require('prop-types')
import {
    requireNativeComponent,
    View,
    StyleSheet,
    NativeModules,
    Platform
} from 'react-native';
const _StreamingManager = NativeModules.HorStreamingManager
const styles = StyleSheet.create({
    base: {
        overflow: 'hidden',
    },
});
class HorStreaming extends Component {
    constructor(props, context) {
        super(props, context);
        this._onConnectError = this._onConnectError.bind(this);
        this._onConnectSuccess = this._onConnectSuccess.bind(this);
        this._onConnectTimeout = this._onConnectTimeout.bind(this);
        this._onNetworkSlow = this._onNetworkSlow.bind(this);
    }

    setNativeProps(nativeProps) {
        this._root.setNativeProps(nativeProps);
    }

    _assignRoot = (component) => {
        this._root = component;
    };

    _onConnectError(event) {
        this.props.onConnectError && this.props.onConnectError(event.nativeEvent);
    }

    _onConnectSuccess(event) {
        this.props.onConnectSuccess && this.props.onConnectSuccess(event.nativeEvent);
    }

    _onConnectTimeout(event) {
        this.props.onConnectTimeout && this.props.onConnectTimeout(event.nativeEvent);
    }

    _onNetworkSlow(event) {
        this.props.onNetworkSlow && this.props.onNetworkSlow(event.nativeEvent);
    }

    static startPush() {
        _StreamingManager.startPush()
    }

    static destoryPush() {
        _StreamingManager.closeStreaming()
    }

    static reconnectPush() {
        Platform.OS === 'ios' ? _StreamingManager.reconnectPush() : false
    }

    static restartPush() {
        Platform.OS === 'ios' ? _StreamingManager.restartPush() : false
    }

    static switchCamera() {
        _StreamingManager.switchCamera()
    }

    static pausePush() {
        _StreamingManager.pausePush()
    }

    static setFlash(){
        _StreamingManager.setFlash()
    }

    static setResolution(resolution) {
        _StreamingManager.setResolution(resolution)
    }

    static setBeautyWhite(beauty) {
        _StreamingManager.setBeautyWhite(beauty)
    }

    static setBeautyOn(beautyOn) {
        _StreamingManager.setBeautyOn(beautyOn)
    }


    render() {
        const nativeProps = Object.assign({}, this.props);
        Object.assign(nativeProps, {
            style: [styles.base, nativeProps.style],
            onConnectError: this._onConnectError,
            onConnectSuccess: this._onConnectSuccess,
            onConnectTimeout: this._onConnectTimeout,
            onNetworkSlow: this._onNetworkSlow,
        });
        return (
            <RCTHorStreaming
                ref={this._assignRoot}
                {...nativeProps}/>
        )
    }
}

HorStreaming.propTypes = {
    rtmpURL: PropTypes.string,
    orientation: PropTypes.bool,//横竖屏
    muted: PropTypes.bool,
    beauty: PropTypes.number,
    exposure: PropTypes.number,
    frameRate: PropTypes.number,
    resolution: PropTypes.number,
    beautyBuffing: PropTypes.number,
    beautyRuddy: PropTypes.number,
    beautySaturation: PropTypes.number,
    beautyWhite: PropTypes.number,
    beautyBrightness: PropTypes.number,
    onConnectError: PropTypes.func,
    onConnectSuccess: PropTypes.func,
    onConnectTimeout: PropTypes.func,
    onNetworkSlow: PropTypes.func,
    ...View.propTypes,
}
const RCTHorStreaming = requireNativeComponent('RCTHorStreaming', HorStreaming);

module.exports = HorStreaming;
