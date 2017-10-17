using System;
using System.Collections.Generic;
using System.Linq;
using System.ServiceModel;
using System.Text;
using System.Threading.Tasks;
using ch.cyberduck.core;

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
        void QuickConnect(string url);

        [OperationContract]
        void RegisterBookmark(string bookmarkPath);

        [OperationContract]
        void RegisterProfile(string profilePath);

        [OperationContract]
        void RegisterRegistration(string registrationPath);
    }
}
