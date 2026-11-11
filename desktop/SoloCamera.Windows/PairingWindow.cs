using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Threading;
using QRCoder;
namespace SoloCamera.Windows;
public sealed class PairingWindow:Window {
 readonly QrPairing pairing;readonly DispatcherTimer timer=new(){Interval=TimeSpan.FromMilliseconds(400)};
 public PairingWindow(Action<string,string> paired){Title="Pair your phone";Width=440;Height=570;ResizeMode=ResizeMode.NoResize;WindowStartupLocation=WindowStartupLocation.CenterOwner;Background=new SolidColorBrush(Color.FromRgb(19,22,29));Foreground=Brushes.White;var panel=new StackPanel{Margin=new Thickness(22)};Content=panel;panel.Children.Add(new TextBlock{Text="Scan. Approve. Go live.",FontSize=24,FontWeight=FontWeights.Bold});panel.Children.Add(new TextBlock{Text="Camera: Solo Camera → Webcam. Screen demo: start Live screen demo in Screen recording, then Pair Windows. Use the same Wi-Fi or hotspot.",TextWrapping=TextWrapping.Wrap,Margin=new Thickness(0,12,0,12)});var image=new Image{Height=290,Width=290};panel.Children.Add(image);var hint=new TextBlock{TextWrapping=TextWrapping.Wrap,Margin=new Thickness(0,10,0,8)};panel.Children.Add(hint);pairing=new((host,key)=>Dispatcher.InvokeAsync(()=>{paired(host,key);Close();}));var addresses=QrPairing.Addresses();var choice=new ComboBox{ItemsSource=addresses,Margin=new Thickness(0,3,0,3)};panel.Children.Add(choice);void Show(){if(choice.SelectedItem is not string address)return;image.Source=CreateQrBitmap(pairing.Code(address));}choice.SelectionChanged+=(_,_)=>Show();if(addresses.Length>0)choice.SelectedIndex=0;else hint.Text="Connect this computer to Wi-Fi or your phone hotspot first.";timer.Tick+=(_,_)=>{if(addresses.Length>0)hint.Text=pairing.Status;};timer.Start();Closed+=(_,_)=>{timer.Stop();pairing.Dispose();};}
 public static BitmapSource CreateQrBitmap(string code){using var generator=new QRCodeGenerator();using var data=generator.CreateQrCode(code,QRCodeGenerator.ECCLevel.M);int size=data.ModuleMatrix.Count,scale=6,width=size*scale;byte[] pixels=new byte[width*width];for(int y=0;y<width;y++)for(int x=0;x<width;x++)pixels[y*width+x]=data.ModuleMatrix[y/scale][x/scale]?(byte)0:(byte)255;var bitmap=BitmapSource.Create(width,width,96,96,PixelFormats.Gray8,null,pixels,width);bitmap.Freeze();return bitmap;}}

