using System.Runtime.InteropServices;
using System.Runtime.InteropServices.ComTypes;
namespace SoloCamera.Windows;
public static class CameraDevices {
 [ComImport,Guid("29840822-5B84-11D0-BD3B-00A0C911CE86"),InterfaceType(ComInterfaceType.InterfaceIsIUnknown)] interface ICreateDevEnum {[PreserveSig]int CreateClassEnumerator(ref Guid type,out IEnumMoniker enumerator,int flags);}
 [ComImport,Guid("55272A00-42CB-11CE-8135-00AA004BB851"),InterfaceType(ComInterfaceType.InterfaceIsIUnknown)] interface IPropertyBag {[PreserveSig]int Read([MarshalAs(UnmanagedType.LPWStr)]string name,[MarshalAs(UnmanagedType.Struct)]out object value,IntPtr log);[PreserveSig]int Write(string name,ref object value);}
 public static string[] List(){object device=Activator.CreateInstance(Type.GetTypeFromCLSID(new Guid("62BE5D10-60EB-11D0-BD3B-00A0C911CE86"))!)!;try{Guid category=new("860BB310-5D01-11D0-BD3B-00A0C911CE86");if(((ICreateDevEnum)device).CreateClassEnumerator(ref category,out var list,0)!=0)return [];var names=new List<string>();try{var one=new IMoniker[1];while(list.Next(1,one,IntPtr.Zero)==0){Guid iid=new("55272A00-42CB-11CE-8135-00AA004BB851");one[0].BindToStorage(null!,null,ref iid,out var bag);try{((IPropertyBag)bag).Read("FriendlyName",out var name,IntPtr.Zero);names.Add(name.ToString()!);}finally{Marshal.ReleaseComObject(bag);Marshal.ReleaseComObject(one[0]);}}}finally{Marshal.ReleaseComObject(list);}return names.ToArray();}finally{Marshal.ReleaseComObject(device);}}
}

