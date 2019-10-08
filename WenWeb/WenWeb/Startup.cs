using Microsoft.Owin;
using Owin;

[assembly: OwinStartupAttribute(typeof(WenWeb.Startup))]
namespace WenWeb
{
    public partial class Startup
    {
        public void Configuration(IAppBuilder app)
        {
            ConfigureAuth(app);
        }
    }
}
