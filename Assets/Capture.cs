using System.Collections;
using System.Collections.Generic;
using System.IO;
using UnityEngine;

public class Capture : MonoBehaviour
{
    IEnumerator DoCapture()
    {
        yield return new WaitForEndOfFrame();

        //ScreenCapture.CaptureScreenshot(Application.persistentDataPath + "/capture.png");

        Texture2D tex = new Texture2D(Screen.width, Screen.height, TextureFormat.RGBA32, false);
        tex.ReadPixels(new Rect(0, 0, Screen.width, Screen.height), 0, 0);
        byte[] bytes = tex.EncodeToPNG();
        File.WriteAllBytes(Application.persistentDataPath + "/capture.png", bytes);
        Debug.Log(Application.persistentDataPath + "/capture.png");
    }


    void OnGUI()
    {
        if (GUI.Button(new Rect(20, 20, 200, 40), "Capture"))
        {
            StartCoroutine(DoCapture());
        }
    }
}
