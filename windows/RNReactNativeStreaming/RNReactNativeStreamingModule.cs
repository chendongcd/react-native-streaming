using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace React.Native.Streaming.RNReactNativeStreaming
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNReactNativeStreamingModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNReactNativeStreamingModule"/>.
        /// </summary>
        internal RNReactNativeStreamingModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNReactNativeStreaming";
            }
        }
    }
}
