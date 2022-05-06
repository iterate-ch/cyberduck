using System;
using System.ServiceModel;

namespace Ch.Cyberduck.Ui.Core.Contracts
{
    [ServiceContract]
    public interface ICyberduck
    {
        [OperationContract]
        void Connect();

        [OperationContract]
        void NewInstance();

        [OperationContract]
        bool OAuth(Uri result);

        [OperationContract]
        void QuickConnect(string url);

        [OperationContract]
        void RegisterBookmark(string bookmarkPath);

        [OperationContract]
        void RegisterProfile(string profilePath);

        [OperationContract]
        void RegisterRegistration(string registrationPath);
    }
}
